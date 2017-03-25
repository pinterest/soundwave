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
import com.pinterest.cmp.cmdb.aws.CloudInstanceStore;
import com.pinterest.cmp.cmdb.bean.EsInstanceCountRecord;
import com.pinterest.cmp.cmdb.elasticsearch.EsInstanceCounterStore;
import com.pinterest.cmp.cmdb.elasticsearch.InstanceCounterStore;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceLifecycleType;
import com.amazonaws.services.ec2.model.ReservedInstanceState;
import com.amazonaws.services.ec2.model.ReservedInstances;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class DailyInstanceCountPerTypeJob implements Callable<Boolean> {

  private static final Logger logger =
      LoggerFactory.getLogger(DailyInstanceCountPerTypeJob.class);
  private final CloudInstanceStore cloudInstanceStore;
  private final InstanceCounterStore instanceCounterStore;
  private final Region region;

  public DailyInstanceCountPerTypeJob(CloudInstanceStore cloudInstanceStore,
                                      EsInstanceCounterStore instanceCounterStore,
                                      Region region) {

    Preconditions.checkNotNull(cloudInstanceStore);
    Preconditions.checkNotNull(instanceCounterStore);
    Preconditions.checkNotNull(region);

    this.cloudInstanceStore = cloudInstanceStore;
    this.instanceCounterStore = instanceCounterStore;
    this.region = region;
  }

  @Override
  public Boolean call() throws Exception {

    OperationStats operationStats = new OperationStats("job", "DailyInstanceCountPerTypeJob");
    Boolean ret = calculateDailyInstanceCounts();

    if (ret) {
      logger.info("DailyInstanceCountPerTypeJob completed successfully");
      operationStats.succeed();
    } else {
      logger.error("DailyInstanceCountPerTypeJob failed");
      operationStats.failed();
    }
    return ret;

  }

  private Boolean calculateDailyInstanceCounts() {

    try {

      DateTime utcNow = DateTime.now(DateTimeZone.UTC);

      List<Instance> instances = cloudInstanceStore.getInstances(region);
      List<ReservedInstances> reservedInstances = cloudInstanceStore.getReservedInstances(region);

      // Generate instance counts per type per Availability zone
      List<EsInstanceCountRecord> instanceCountRecords =
          getInstanceCountRecords(instances, reservedInstances, utcNow);
      logger.info("Number of instance count records {}", instanceCountRecords.size());

      // Insert records into cmdb store.
      instanceCounterStore.bulkInsert(instanceCountRecords);
      logger.info("Bulk insert succeeded for instance count records");

      return true;

    } catch (Exception e) {

      logger.error(ExceptionUtils.getRootCauseMessage(e));
      return false;
    }
  }

  /**
   * Get instance count records for a specfic zone.
   *
   * @param instances         a list of active instances
   * @param reservedInstances a list of active reservations
   * @return a list of instancecount records for that zone.
   * @throws Exception
   */
  public List<EsInstanceCountRecord> getInstanceCountRecords(
      List<Instance> instances,
      List<ReservedInstances> reservedInstances,
      DateTime utcNow) throws Exception {

    List<EsInstanceCountRecord> ret = new ArrayList<>();
    Map<String, Map<String, Integer>> instanceTypeCounter = new HashMap<>();

    // Process and add all zones Running instances first.
    for (Instance instance : instances) {

      String instanceType = instance.getInstanceType();
      boolean isSpotInstance = StringUtils.equalsIgnoreCase(
          instance.getInstanceLifecycle(), InstanceLifecycleType.Spot.toString());

      if (instanceTypeCounter.containsKey(instanceType)) {

        instanceTypeCounter.get(instanceType).put("active",
            instanceTypeCounter.get(instanceType).get("active") + 1);

        if (isSpotInstance) {
          instanceTypeCounter.get(instanceType).put("spot",
              instanceTypeCounter.get(instanceType).get("spot") + 1);
        }

      } else {

        // Instance type doesnot exist in counter. initialize its values
        instanceTypeCounter.put(instanceType, new HashMap<>());
        instanceTypeCounter.get(instanceType).put("active", 1);
        instanceTypeCounter.get(instanceType).put("reserved", 0);
        instanceTypeCounter.get(instanceType).put("ondemand", 0);
        instanceTypeCounter.get(instanceType).put("unused", 0);
        if (isSpotInstance) {
          instanceTypeCounter.get(instanceType).put("spot", 1);
        } else {
          instanceTypeCounter.get(instanceType).put("spot", 0);
        }
      }
    }

    // Process all reserved instances here
    for (ReservedInstances instance : reservedInstances) {

      String instanceType = instance.getInstanceType();
      int instanceCount = instance.getInstanceCount();
      boolean isActive = StringUtils.equalsIgnoreCase(
          instance.getState(), ReservedInstanceState.Active.toString());

      if (isActive) {
        instanceTypeCounter.get(instanceType).put("reserved",
            instanceTypeCounter.get(instanceType).get("reserved") + instanceCount);
      }
    }

    // For each instance type in a given zone create one count record
    for (String instanceType : instanceTypeCounter.keySet()) {

      int onDemandCount = instanceTypeCounter.get(instanceType).get("active")
          - instanceTypeCounter.get(instanceType).get("reserved")
          - instanceTypeCounter.get(instanceType).get("spot");

      if (onDemandCount < 0) {
        // Reserved instances are unused
        instanceTypeCounter.get(instanceType).put("unused", Math.abs(onDemandCount));
        instanceTypeCounter.get(instanceType).put("ondemand", 0);

      } else {
        instanceTypeCounter.get(instanceType).put("unused", 0);
        instanceTypeCounter.get(instanceType).put("ondemand", onDemandCount);
      }

      // Unused is set to 0. No way to get how many we have bought.
      EsInstanceCountRecord record = new EsInstanceCountRecord(
          region.toString(),
          instanceType,
          instanceTypeCounter.get(instanceType).get("reserved"),
          instanceTypeCounter.get(instanceType).get("active"),
          instanceTypeCounter.get(instanceType).get("spot"),
          instanceTypeCounter.get(instanceType).get("ondemand"),
          instanceTypeCounter.get(instanceType).get("unused"), utcNow.toDate());

      ret.add(record);
    }
    return ret;
  }

  // End of class
}
