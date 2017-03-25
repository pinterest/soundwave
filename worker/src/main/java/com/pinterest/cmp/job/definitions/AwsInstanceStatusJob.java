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
import com.pinterest.cmp.cmdb.aws.AwsStatus;
import com.pinterest.cmp.cmdb.aws.CloudInstanceStore;
import com.pinterest.cmp.cmdb.aws.Ec2InstanceStore;
import com.pinterest.cmp.cmdb.bean.EsAwsStatus;
import com.pinterest.cmp.cmdb.elasticsearch.CmdbInstanceStore;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.InstanceStatusEvent;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * This is used to fetch instance state from AWS.
 * If the system status or instance status is not returned as ok
 * then add that tag to the instance in Cmdb Database
 */
public class AwsInstanceStatusJob implements Callable<Boolean> {

  private static final Logger logger = LoggerFactory.getLogger(AwsInstanceStatusJob.class);
  private final CloudInstanceStore cloudInstanceStore;
  private final Region region;
  private CmdbInstanceStore cmdbInstanceStore;
  private int total;
  private int badInstanceCount;

  public AwsInstanceStatusJob(CloudInstanceStore cloudInstanceStore,
                              CmdbInstanceStore cmdbInstanceStore,
                              Region region) {

    Preconditions.checkNotNull(cmdbInstanceStore);
    Preconditions.checkNotNull(cloudInstanceStore);
    Preconditions.checkNotNull(region);

    this.cmdbInstanceStore = cmdbInstanceStore;
    this.cloudInstanceStore = cloudInstanceStore;
    this.region = region;
    this.total = 0;
    this.badInstanceCount = 0;
  }

  /**
   * Computes a result, or throws an exception if unable to do so.
   *
   * @return computed result
   * @throws Exception if unable to compute a result
   */
  @Override
  public Boolean call() throws Exception {
    OperationStats op = new OperationStats("job", "AwsInstanceStatusJob");
    boolean ret = checkInstancesStatus();
    if (ret) {
      op.succeed();
    } else {
      op.failed();
    }
    return ret;
  }

  private Boolean checkInstancesStatus() {

    try {
      // Type cast cloudInstanceStore to use ec2 functions
      Ec2InstanceStore ec2Store = (Ec2InstanceStore) cloudInstanceStore;

      // Fetch aws status for all instances in the given region
      List<InstanceStatus> statuses = ec2Store.describeInstancesStatusAsync(region);

      if (statuses == null) {
        logger.warn("AWS did not return any InstanceStatus");
        return false;
      }

      // Creates a reverse map of id to Status
      Map<String, InstanceStatus> idToInstanceStatus = new HashMap<>(statuses.size());

      for (InstanceStatus status : statuses) {
        idToInstanceStatus.put(status.getInstanceId(), status);
      }

      // Iterate over all Running CmdbInstances & add status updated ones to list
      Iterator<EsAwsStatus> iterator =
          cmdbInstanceStore.getRunningAndTerminatedAwsStatus(region, 1);

      List<EsAwsStatus> updateInstanceList = getUpdatedInstanceList(iterator, idToInstanceStatus);

      logger.info("Number of instances found to update AwsStatus = {}", updateInstanceList.size());

      // Log the count of healthy instances & unhealthy ones.
      logger.info("Total count: = {} and unhealthy nodes count with status"
          + " not ok = {}", total, badInstanceCount);

      // Update all instances healthy + non healthy in CMDB
      logger.info("Starting a bulk update for awsStatus tag...");
      cmdbInstanceStore.bulkUpdateAwsStatus(updateInstanceList);

      // Reset the counters to 0
      total = 0;
      badInstanceCount = 0;
      logger.info("Re initialized total to {} and badInstanceCount to {}", total, badInstanceCount);

      return true;

    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;

  }

  private List<EsAwsStatus> getUpdatedInstanceList(Iterator<EsAwsStatus> iterator,
                                                   Map<String, InstanceStatus> idToInstanceStatus)
      throws Exception {

    List<EsAwsStatus> updateInstanceList = new ArrayList<>();

    while (iterator.hasNext()) {

      EsAwsStatus cmdbInstanceStatus = iterator.next();
      String instanceId = cmdbInstanceStatus.getId();

      // Consider this cmdbInstance for update only if aws returned a Status for this instanceID
      if (idToInstanceStatus.containsKey(instanceId)) {

        InstanceStatus status = idToInstanceStatus.get(instanceId);
        String instanceStatus = status.getInstanceStatus().getStatus();
        String systemStatus = status.getSystemStatus().getStatus();
        List<InstanceStatusEvent> events = status.getEvents();

        total++;

        if (events.size() > 0) {

          List<String> codes = new ArrayList<>();

          for (InstanceStatusEvent event : events) {

            // If description contains completed then ignore.
            if (!event.getDescription().contains("Completed")) {

              codes.add(event.getCode());
            }
          }

          if (codes.size() > 0) {

            logger.info("Instance {} with non-zero events reported by aws codes:{}",
                instanceId, codes);

            // Create aws status hierarchy
            AwsStatus awsStatus = new AwsStatus();
            awsStatus.setRaw(status);
            awsStatus.setCodes(codes);

            // Update awsStatus of the instance
            cmdbInstanceStatus.setAwsStatus(awsStatus);

            // Add the instance to bulk update list
            updateInstanceList.add(cmdbInstanceStatus);

          } else if (cmdbInstanceStatus.getAwsStatus() != null) {

            // Clear the awsStatus because no new codes are reported
            cmdbInstanceStatus.setAwsStatus(null);

            // Add instance to bulk update call
            updateInstanceList.add(cmdbInstanceStatus);
          }


        } else if (!StringUtils.equalsIgnoreCase("ok", instanceStatus)
            || !StringUtils.equalsIgnoreCase("ok", systemStatus)) {

          // one of the two statuses was returned as not ok by aws
          logger.warn("Unhealthy instance reported by aws instance Id : {},"
                  + " instanceStatus : {}, systemStatus : {}",
              instanceId, instanceStatus, systemStatus);

          badInstanceCount++;

          // Add a new field to CMDB Instance Object called aws status
          List<String> codes = new ArrayList<>();

          // Get all event codes
          for (InstanceStatusEvent event : events) {
            codes.add(event.getCode());
          }

          // Create the aws Status hierarchy
          AwsStatus awsStatus = new AwsStatus();
          awsStatus.setRaw(status);
          awsStatus.setCodes(codes);

          // Update awsStatus of the instance
          cmdbInstanceStatus.setAwsStatus(awsStatus);

          // Add the instance to bulk update list
          updateInstanceList.add(cmdbInstanceStatus);

        } else if (cmdbInstanceStatus.getAwsStatus() != null) {

          // If both instanceStatus and SystemStatus is "ok"
          // then check if instance has an awsStatus attribute.

          // This was an unhealthy instance in last check
          logger.info("Previously marked unhealthy instance is now reported ok {}", instanceId);

          // Set aws Status to null.
          // This is mimicked from previous job script. It should be set to status ok
          cmdbInstanceStatus.setAwsStatus(null);

          // Add the instance to bulk update list
          updateInstanceList.add(cmdbInstanceStatus);

        }

      }

    }

    return updateInstanceList;

  }

  // Class ends
}
