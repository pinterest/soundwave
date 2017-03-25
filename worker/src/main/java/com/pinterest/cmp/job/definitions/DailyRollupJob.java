/**
 * Copyright 2017 Pinterest, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pinterest.cmp.job.definitions;

import com.pinterest.cmp.OperationStats;
import com.pinterest.cmp.cmdb.aws.DailySnapshotStore;
import com.pinterest.cmp.cmdb.aws.DailySnapshotStoreFactory;
import com.pinterest.cmp.cmdb.bean.EsDailySnapshotInstance;
import com.pinterest.cmp.cmdb.elasticsearch.CmdbInstanceStore;
import com.pinterest.cmp.cmdb.utils.JsonCompareUtil;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Regularly check and update the instance values to the daily snapshot index
 */
public class DailyRollupJob implements Callable<Boolean> {

  private static final Logger logger = LoggerFactory.getLogger(DailyRollupJob.class);

  private CmdbInstanceStore cmdbStore;
  private DailySnapshotStoreFactory dailySnapshotStoreFactory;

  public DailyRollupJob(CmdbInstanceStore cmdbStore,
                        DailySnapshotStoreFactory dailySnapshotStoreFactory) {

    this.cmdbStore = cmdbStore;
    this.dailySnapshotStoreFactory = dailySnapshotStoreFactory;
  }

  public Boolean call() throws Exception {
    OperationStats op = new OperationStats("job", "DailyRollupJob");
    try {
      Iterator<EsDailySnapshotInstance>
          cmdbInstances =
          cmdbStore.getRunningAndRecentTerminatedInstances(0);

      DailySnapshotStore dailySnapshotStore = dailySnapshotStoreFactory.getDailyStore(
          DateTime.now(DateTimeZone.UTC));
      Iterator<EsDailySnapshotInstance>
          currentDailySnapshot =
          dailySnapshotStore.getSnapshotInstances();

      RollupUpdateSet updateSet = getUpdateset(cmdbInstances, currentDailySnapshot);
      logger.info("Have {} inserts and {} updates", updateSet.getInsertList().size(),
          updateSet.getUpdateList().size());

      dailySnapshotStore.bulkInsert(updateSet.getInsertList());
      dailySnapshotStore.bulkUpdate(updateSet.getUpdateList());

      op.succeed();
      return true;
    } catch (Exception ex) {
      op.failed();
      throw ex;
    }
  }


  //Find out the set need to update. The size is small enough to be put both in memory now (<30K
  // entries). So do compare in memory now
  public RollupUpdateSet getUpdateset(Iterator<EsDailySnapshotInstance> cmdbInstances,
                                      Iterator<EsDailySnapshotInstance>
                                          currentSnapshot) {
    RollupUpdateSet ret = new RollupUpdateSet();
    Map<String, EsDailySnapshotInstance> current = buildMap(currentSnapshot);
    int cmdbInstanceCount = 0;
    while (cmdbInstances.hasNext()) {
      EsDailySnapshotInstance inst = cmdbInstances.next();
      EsDailySnapshotInstance existing = current.get(inst.getId());
      if (existing == null) {
        ret.getInsertList().add(inst);
      } else {
        try {
          Map<String, Object[]> diff = JsonCompareUtil.findDiff(inst, existing);
          if (diff.size() > 0) {
            logger.info("Find out a diff {} for {}",
                JsonCompareUtil.DumpMapper.writeValueAsString(diff), existing.getId());
            //inst is from CMDB store. Need to set to the existing version
            inst.setVersion(existing.getVersion());
            ret.getUpdateList()
                .add(inst); //TODO, we may just update the doc with the diff properites
          }
        } catch (Exception e) {
          logger.error("Error in finddiff in {} with error {}", existing.getId(),
              ExceptionUtils.getRootCauseMessage(e));
        }

      }
      cmdbInstanceCount++;
    }

    logger.info("CMDB total instances {}. DailySnapshot total instances {}", cmdbInstanceCount,
        current.size());
    return ret;
  }

  private Map<String, EsDailySnapshotInstance> buildMap(
      Iterator<EsDailySnapshotInstance> instanceIterator) {
    HashMap<String, EsDailySnapshotInstance> ret = new HashMap<>();
    while (instanceIterator.hasNext()) {
      EsDailySnapshotInstance inst = instanceIterator.next();
      ret.put(inst.getId(), inst);
    }
    return ret;
  }


  public class RollupUpdateSet {

    private List<EsDailySnapshotInstance> insertList = new ArrayList<>();
    private List<EsDailySnapshotInstance> updateList = new ArrayList<>();

    public List<EsDailySnapshotInstance> getInsertList() {
      return insertList;
    }

    public List<EsDailySnapshotInstance> getUpdateList() {
      return updateList;
    }
  }
}
