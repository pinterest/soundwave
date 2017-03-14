package com.pinterest.cmp.job.definitions;

import com.pinterest.cmp.AwsServiceTagUpdater;
import com.pinterest.cmp.OperationStats;
import com.pinterest.cmp.StatsUtil;
import com.pinterest.cmp.UploadTagsGenerator;
import com.pinterest.cmp.cmdb.aws.BasicEsInstanceFactory;
import com.pinterest.cmp.cmdb.aws.CloudInstanceStore;
import com.pinterest.cmp.cmdb.bean.EsInstance;
import com.pinterest.cmp.cmdb.bean.State;
import com.pinterest.cmp.cmdb.elasticsearch.CmdbInstanceStore;
import com.pinterest.cmp.cmdb.elasticsearch.EsMapper;
import com.pinterest.cmp.cmdb.utils.JsonCompareUtil;
import com.pinterest.cmp.config.Configuration;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Tag;
import com.google.common.base.Preconditions;
import com.twitter.ostrich.stats.Stats;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ReconcileWithAwsJob implements Callable<Boolean> {

  private static final Logger logger = LoggerFactory.getLogger(ReconcileWithAwsJob.class);
  private static final Set<String>
      EXCLUDEDPROPERTIES =
      new HashSet<>(
          Arrays.asList("aws_status", "created_time", "updated_time", "terminated_time", "pkgs",
              "facts", "service_mapping", "usage_tag"));

  private static final Set<String> NONRETREIEVEPROPERTIES = new HashSet<>(
      Arrays.asList("pkgs", "facts")
  );

  private CloudInstanceStore cloudInstanceStore;
  private CmdbInstanceStore cmdbStore;
  private Region region;

  public ReconcileWithAwsJob(CloudInstanceStore cloudInstanceStore, CmdbInstanceStore cmdbStore,
                             Region region) {
    Preconditions.checkNotNull(cloudInstanceStore);
    Preconditions.checkNotNull(cmdbStore);
    this.cloudInstanceStore = cloudInstanceStore;
    this.cmdbStore = cmdbStore;
    this.region = region;
  }

  public static Map<List<Tag>, List<String>> getBatchUpdateTags(
      List<Pair<String, List<Tag>>> updateTags) {
    HashMap<List<Tag>, List<String>> batches = new HashMap<>();
    for (Pair<String, List<Tag>> updateTag : updateTags) {
      List<String> resource = batches.get(updateTag.getRight());
      if (resource != null) {
        batches.get(updateTag.getRight()).add(updateTag.getLeft());
      } else {
        batches.put(updateTag.getRight(), new ArrayList<>(Arrays.asList(updateTag.getLeft())));
      }
    }
    return batches;
  }

  /**
   *
   * @return
   * @throws Exception
   */
  @Override
  public Boolean call() throws Exception {
    OperationStats op = new OperationStats("job", "ReconcileWithAwsJob");
    try {
      ReconcileUpdateSet updateSet = getUpdateSet(this.cloudInstanceStore, this.cmdbStore);
      this.cmdbStore.bulkInsert(updateSet.getInsertList());
      this.cmdbStore.bulkUpdate(updateSet.getUpdateList());

      logger.info("Upload tags to EC2 with {} updates", updateSet.uploadEc2Tags.size());
      Map<List<Tag>, List<String>> batchUpdates = this.getBatchUpdateTags(updateSet.uploadEc2Tags);
      logger.info("Create {} batch updates", batchUpdates.size());
      for (Map.Entry<List<Tag>, List<String>> kvp : batchUpdates.entrySet()) {
        try {
          cloudInstanceStore.setTagsForInstances(kvp.getValue(), kvp.getKey());
        } catch (Exception ex) {
          logger.warn("Upload tags to EC2 failed with {}", ex.getMessage());
        }
      }

      op.succeed();
      return true;
    } catch (Exception ex) {
      op.failed();
      throw ex;
    }
  }

  public ReconcileUpdateSet getUpdateSet(CloudInstanceStore cloudInstanceStore,
                                         CmdbInstanceStore cmdbStore)
      throws Exception {

    ReconcileUpdateSet updateSet = new ReconcileUpdateSet();
    Region region = this.region;

    DateTime utcNow = DateTime.now(DateTimeZone.UTC);
    DateTime fiveMinutesBeforeNow = utcNow.minusMinutes(5);

    //Starting async retrieve on Ec2 date
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    try {
      Future<List<Instance>> futureEc2 = executorService.submit(
          new Callable<List<Instance>>() {
            @Override
            public List<Instance> call() throws Exception {
              return cloudInstanceStore.getInstances(region);
            }
          }
      );

      Map<String, EsInstance>
          currentRunningInstances =
          buildMap(cmdbStore.getRunningInstances(this.region, getFields()));

      HashSet<String> cmdbRunningInstances = new HashSet<>();
      cmdbRunningInstances.addAll(currentRunningInstances.keySet());

      List<Instance> ec2Instances = futureEc2.get(10, TimeUnit.MINUTES);
      BasicEsInstanceFactory
          instanceFactory =
          new BasicEsInstanceFactory();
      instanceFactory.setCloudInstanceStore(cloudInstanceStore);

      logReconcileStats(ec2Instances, currentRunningInstances.values());

      logger.info("Ec2 return {} instances. CMDB shows {} running instances", ec2Instances.size(),
          cmdbRunningInstances.size());
      int total = 0;
      //Reconcilation is an approximate process. We are comparing two seperate stores are updated
      //concurrently. There is no global timestamp or version to compare. Even worse, terminated
      //instances are not returned from Ec2. The code below is trying to keep best guess and it
      // tries
      //to avoid the case overwrite the new update value to old value as much as possible
      for (Instance ec2Instance : ec2Instances) {
        try {
          total++;
          cmdbRunningInstances.remove(ec2Instance.getInstanceId());

          if (fiveMinutesBeforeNow.isBefore(new DateTime(ec2Instance.getLaunchTime()))) {
            logger.info("Ignore recently launched instance {} in recouncile",
                ec2Instance.getInstanceId());
            continue;
          }

          //EC2 may return terminated instances whose information is incomplete. Only
          //recouncile running instances.
          if (!isRunningOrPending(ec2Instance.getState())) {
            logger.info("Ignore non running or pending instance {} from ec2 in recouncile",
                ec2Instance.getInstanceId());
            continue;
          }

          EsInstance ec2Value = instanceFactory.createFromEC2(ec2Instance);

          if (currentRunningInstances.containsKey(ec2Instance.getInstanceId())) {
            EsInstance cmdbValue = currentRunningInstances.get(ec2Instance.getInstanceId());
            if (ec2Value.getState().equals(State.TERMINATED.toString())) {
              //Once instance has been terminated. It may only contain partial information. It'd
              // be better to not override the other properties but just set State to be terminated.
              cmdbValue.setState(State.TERMINATED.toString());
              cmdbValue.setTerminateTime(DateTime.now(DateTimeZone.UTC).toDate());
              updateSet.getUpdateList().add(cmdbValue);

            } else {
              checkDiff(ec2Value, cmdbValue, updateSet, fiveMinutesBeforeNow);
            }
          } else {
            //The ec2 instance is not in running. There are two cases:
            // 1. This instance hasn't been added ever
            // 2. This instance has been terminated.
            EsInstance cmdbValue = cmdbStore.getInstanceById(ec2Instance.getInstanceId());
            if (cmdbValue == null) {
              updateSet.getInsertList().add(ec2Value);
            } else {
              checkDiff(ec2Value, cmdbValue, updateSet, fiveMinutesBeforeNow);
            }
          }
          if (total % 1000 == 0) {
            logger.info("Processed {} instances", total);
          }
        } catch (Exception e) {
          logger.error("Fail to compare instance {} with error {}", ec2Instance.getInstanceId(),
              ExceptionUtils.getRootCauseMessage(e));
          logger.error(ExceptionUtils.getFullStackTrace(e));
        }
      }

      if (cmdbRunningInstances.size() > 0) {
        logger.info("{} instances in CMDB but cannot find in Ec2", cmdbRunningInstances.size());
        for (String id : cmdbRunningInstances) {
          EsInstance cmdbValue = cmdbStore.getInstanceById(id);
          if (cmdbValue.getAwsLaunchTime().before(fiveMinutesBeforeNow.toDate())) {
            cmdbValue.setState(State.TERMINATED.toString());
            cmdbValue.setUpdatedTime(utcNow.toDate());
            cmdbValue.setTerminateTime(utcNow.toDate());
            updateSet.getUpdateList().add(cmdbValue);
          }
        }
      }

      if (Configuration.getProperties().getBoolean("send_service_mapping_to_aws", false)) {
        //Compute what need to upload to Ec2
        computeUploadTags(ec2Instances, currentRunningInstances, updateSet);
      }

    } finally {
      executorService.shutdownNow();
    }

    return updateSet;
  }

  public void checkDiff(EsInstance ec2Value, EsInstance cmdbValue, ReconcileUpdateSet updateSet,
                        DateTime updateThreshold) throws Exception {
    //cmdb already has the value
    Map<String, Object[]> diffs = JsonCompareUtil.findDiff(ec2Value, cmdbValue, EXCLUDEDPROPERTIES);

    if (diffs.size() > 0) {
      logger.info("Find out a diff {} for {}",
          JsonCompareUtil.DumpMapper.writeValueAsString(diffs), cmdbValue.getId());
      if (cmdbValue.getUpdatedTime()
          .before(updateThreshold.toDate())) {
        //If there is a diff and cmdb value hasn't been updated recently, update per to
        // value
        logger.info("Add {} to update list because it has a diff and not updated recently",
            cmdbValue.getId());
        ec2Value.setVersion(cmdbValue.getVersion());
        updateSet.getUpdateList().add(ec2Value);
      }
    }
  }

  public void computeUploadTags(List<Instance> ec2Instances, Map<String, EsInstance>
      currentRunningInstances, ReconcileUpdateSet updateSet) {
    UploadTagsGenerator tagsGenerator = AwsServiceTagUpdater.getInstance().getTagsGenerator();
    for (Instance inst : ec2Instances) {
      EsInstance esInstance = currentRunningInstances.get(inst.getInstanceId());
      if (esInstance != null) {
        List<Tag> updateTags = tagsGenerator.getUpdateTags(inst, esInstance);
        if (updateTags.size() > 0) {
          updateSet.getUploadEc2Tags().add(new ImmutablePair<>(esInstance.getId(), updateTags));
        }
      }
    }
  }

  private Map<String, EsInstance> buildMap(Iterator<EsInstance> iterator) {
    HashMap<String, EsInstance> ret = new HashMap<>();
    while (iterator.hasNext()) {
      EsInstance esInstance = iterator.next();
      ret.put(esInstance.getId(), esInstance);
    }
    return ret;
  }

  private void logReconcileStats(Collection<Instance> ec2Instances,
                                 Collection<EsInstance> cmdbInstances) {

    int ec2RunningCount = 0;
    for (Instance inst : ec2Instances) {
      if (inst.getState().getCode() == 16) {
        //EC2 API may return some terminated instances. Only get the running count
        ec2RunningCount++;
      }
    }
    Stats.setGauge(StatsUtil.getStatsName("awsreconcile", "ec2TotalRunningCount"), ec2RunningCount);
    Stats.setGauge(StatsUtil.getStatsName("awsreconcile", "cmdbTotalRunningCount"),
        cmdbInstances.size());
    Stats.setGauge(StatsUtil.getStatsName("awsreconcile", "diffcount"),
        Math.abs(ec2RunningCount - cmdbInstances.size()));

    int serviceMappingMissingCount = 0;
    for (EsInstance instance : cmdbInstances) {
      /*if (instance.getServiceMappings() == null || instance.getServiceMappings().length == 0) {
        serviceMappingMissingCount++;
      }*/
    }
    Stats.setGauge(StatsUtil.getStatsName("awsreconcile", "servicemappingmissingcount"),
        serviceMappingMissingCount);

  }

  private boolean isRunningOrPending(InstanceState state) {
    boolean ret = false;
    if (state != null) {
      ret = state.getCode() == 0 || state.getCode() == 16; //0 - pending. 16 -- running
    }
    return ret;
  }

  private String[] getFields() {
    String[] fields = EsMapper.getIncludeFields(cmdbStore.getInstanceClass());
    ArrayList<String> retList = new ArrayList<>();
    for (String field : fields) {
      if (!NONRETREIEVEPROPERTIES.contains(field)) {
        retList.add(field);
      }
    }
    return retList.toArray(new String[0]);
  }

  public class ReconcileUpdateSet {

    private List<EsInstance> insertList = new ArrayList<>();
    private List<EsInstance> updateList = new ArrayList<>();
    private List<Pair<String, List<Tag>>> uploadEc2Tags = new ArrayList<>();


    public List<EsInstance> getInsertList() {
      return insertList;
    }

    public List<EsInstance> getUpdateList() {
      return updateList;
    }

    public List<Pair<String, List<Tag>>> getUploadEc2Tags() {
      return uploadEc2Tags;
    }

  }
}
