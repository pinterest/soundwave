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
package com.pinterest.cmp;


import com.pinterest.cmp.cmdb.aws.CloudInstanceStore;
import com.pinterest.cmp.cmdb.aws.Ec2InstanceStore;
import com.pinterest.cmp.cmdb.aws.Ec2InstanceUpdateHandler;
import com.pinterest.cmp.cmdb.aws.SqsClient;
import com.pinterest.cmp.cmdb.elasticsearch.CmdbInstanceStore;
import com.pinterest.cmp.cmdb.elasticsearch.EsDailySnapshotStoreFactory;
import com.pinterest.cmp.cmdb.elasticsearch.EsInstanceCounterStore;
import com.pinterest.cmp.config.Configuration;
import com.pinterest.cmp.job.ExclusiveRecurringJobExecutor;
import com.pinterest.cmp.job.JobExecutor;
import com.pinterest.cmp.job.JobInfoStore;
import com.pinterest.cmp.job.SqsTriggeredJobExecutor;
import com.pinterest.cmp.job.definitions.AwsInstanceStatusJob;
import com.pinterest.cmp.job.definitions.DailyInstanceCountPerTypeJob;
import com.pinterest.cmp.job.definitions.DailyRollupJob;
import com.pinterest.cmp.job.definitions.ElasticSearchHealthCheckJob;
import com.pinterest.cmp.job.definitions.HealthCheckJob;
import com.pinterest.cmp.job.definitions.ReconcileWithAwsJob;
import com.pinterest.cmp.zookeeper.ZkScheduledJobOwnershipDecider;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class JobManager {

  private static Logger logger = LoggerFactory.getLogger(JobManager.class);
  protected CloudInstanceStore ec2InstanceStore;
  protected CmdbInstanceStore cmdbInstanceStore;
  protected Region awsRegion;
  private List<JobExecutor> executors = new ArrayList<>();
  private JobInfoStore jobInfoStore;


  public JobManager(JobInfoStore jobInfoStore, CloudInstanceStore cloudInstanceStore,
                    CmdbInstanceStore cmdbInstanceStore) {
    ;
    this.jobInfoStore = jobInfoStore;
    this.ec2InstanceStore = cloudInstanceStore;
    this.cmdbInstanceStore = cmdbInstanceStore;
    this.awsRegion = Region.getRegion(
        Regions.fromName(Configuration.getProperties().getString("aws_region", "us-east-1")));

    int numOfSubscribers = Configuration.getProperties().getInt("num_subscriber", 1);
    logger.info("JobManager starts with {} subscribers", numOfSubscribers);
    for (int i = 0; i < numOfSubscribers; i++) {
      Ec2InstanceUpdateHandler
          handler =
          new Ec2InstanceUpdateHandler(cmdbInstanceStore, cloudInstanceStore,
              new EsDailySnapshotStoreFactory());

      SqsClient client = new SqsClient(handler);
      SqsTriggeredJobExecutor
          executor =
          new SqsTriggeredJobExecutor(Configuration.getProperties().getString("update_queue"),
              15, msg -> client.processMessage(msg));
      executor.setMinimumReprocessingDuration(30);
      executors.add(executor);
      this.onEc2HandlerCreate(handler);
    }

    //Add recurring jobs
    addRecurringJobs();
  }

  public void start() {
    for (JobExecutor executor : executors) {
      executor.execute();
    }
  }

  private void addRecurringJobs() {
    try {

      String jobName = "AwsStatusJob_" + awsRegion.getName();
      executors.add(
          new ExclusiveRecurringJobExecutor(jobInfoStore, 60 * 15,
              new AwsInstanceStatusJob(ec2InstanceStore, cmdbInstanceStore, awsRegion),
              jobName,
              ZkScheduledJobOwnershipDecider.buildDecider(jobName)));

      // Instance Type Counter Job
      String instanceCounterJobName = String.format("InstanceTypeCounterJob_%s",
          awsRegion.getName());

      executors.add(
          new ExclusiveRecurringJobExecutor(jobInfoStore, 60 * 60 * 24, // Every 24 hrs
              new DailyInstanceCountPerTypeJob(
                  new Ec2InstanceStore(), new EsInstanceCounterStore(), awsRegion),
              instanceCounterJobName,
              ZkScheduledJobOwnershipDecider.buildDecider(instanceCounterJobName)));

      executors.add(
          new ExclusiveRecurringJobExecutor(jobInfoStore, 60 * 30,
              new DailyRollupJob(cmdbInstanceStore, new EsDailySnapshotStoreFactory()),
              "DailyRollupJob", ZkScheduledJobOwnershipDecider.buildDecider("DailyRollupJob"))
      );

      executors.add(new ExclusiveRecurringJobExecutor(jobInfoStore, 60 * 60,
          new ReconcileWithAwsJob(ec2InstanceStore, cmdbInstanceStore,
              awsRegion), "ReconcileWithAws_" + awsRegion.getName(),
          ZkScheduledJobOwnershipDecider.buildDecider("ReconcileWithAws_" + awsRegion.getName())));

      executors.add(new ExclusiveRecurringJobExecutor(jobInfoStore, 60 * 10, //10 minutes
          new HealthCheckJob(), "HealthCheckJob",
          ZkScheduledJobOwnershipDecider.buildDecider("HealthCheckJob")));

      executors.add(new ExclusiveRecurringJobExecutor(jobInfoStore, 60 * 5, //5 minutes
          new ElasticSearchHealthCheckJob(), "ElasticSearchHealthCheckJob",
          ZkScheduledJobOwnershipDecider.buildDecider("ElasticSearchHealthCheckJob")));


    } catch (Exception ex) {
      logger.error(ExceptionUtils.getRootCauseMessage(ex));
      logger.error(ExceptionUtils.getFullStackTrace(ex));
    }
  }

  protected void registerJobExecutor(JobExecutor executor) {
    Preconditions.checkNotNull(executor);
    this.executors.add(executor);
  }

  protected void onEc2HandlerCreate(Ec2InstanceUpdateHandler handler) {}
}
