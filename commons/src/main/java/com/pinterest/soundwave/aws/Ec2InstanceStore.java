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
package com.pinterest.soundwave.aws;


import com.pinterest.OperationStats;
import com.pinterest.aws.AwsClientFactory;
import com.pinterest.config.Configuration;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeReservedInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeReservedInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceAttribute;
import com.amazonaws.services.ec2.model.InstanceAttributeName;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.ReservedInstances;
import com.amazonaws.services.ec2.model.Tag;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Helper class for EC2
 */
public final class Ec2InstanceStore implements CloudInstanceStore {

  private static final Logger logger = LoggerFactory.getLogger(Ec2InstanceStore.class);
  private static RateLimiter
      awsRateLimiter =
      RateLimiter.create(Configuration.getProperties().getInt("aws_call_ratelimit", 1));

  private AmazonEC2Client defaultClient = AwsClientFactory.createEC2Client(
      Region.getRegion(
          Regions.fromName(Configuration.getProperties().getString("aws_region", "us-east-1")))
  );
  private ConcurrentHashMap<String, AmazonEC2Client> regionClientsCache = new ConcurrentHashMap<>();

  public static Map<String, String> parseEc2UserData(String base64Input) {
    Preconditions.checkNotNull(base64Input);
    HashMap<String, String> ret = new HashMap<>();
    byte[] decodeData = null;
    try {
      decodeData = Base64.getDecoder().decode(base64Input);
    } catch (Exception e) {
      logger
          .warn("Fail to decode User data {} with  with error {}", base64Input,
              ExceptionUtils.getRootCauseMessage(e));
    }
    if (decodeData != null) {
      try {
        String str = IOUtils.toString(decodeData, "UTF-8");
        String[] lines = StringUtils.split(str, '\n');
        for (String line : lines) {
          if (!StringUtils.isEmpty(line) && !line.startsWith("#")) {
            String[] kvp = StringUtils.split(line, ':');
            if (kvp.length == 2) {
              ret.put(StringUtils.trim(kvp[0]), StringUtils.trim(kvp[1]));
            }
          }
        }
      } catch (IOException ioe) {
        logger
            .warn("Fail to parse User data with error {}", ExceptionUtils.getRootCauseMessage(ioe));
      }
    }
    return ret;
  }

  private AmazonEC2Client getClient(Region region) {
    AmazonEC2Client client = regionClientsCache.get(region.getName());
    if (client == null) {
      client = AwsClientFactory.createEC2Client(region);
      regionClientsCache.put(region.getName(), client);
    }
    return client;
  }

  /**
   * Get an EC2 instance from the instance id. It gets the Reservation and loop through the
   * instances
   * under the reservation for find the instane has the given id
   *
   * @param instanceId
   * @return EC2 instance
   * @throws Exception
   */
  @Override
  public Instance getInstance(String instanceId)
      throws Exception {
    Preconditions.checkNotNull(instanceId);
    Preconditions.checkNotNull(defaultClient);
    OperationStats ops = new OperationStats("es2InstanceStore", "getInstance");
    try {
      awsRateLimiter.acquire();
      DescribeInstancesRequest request = new DescribeInstancesRequest();
      request.setInstanceIds(Arrays.asList(instanceId));
      DescribeInstancesResult result = defaultClient.describeInstances(request);

      for (Reservation reservation : result.getReservations()) {
        //Reservation refers to one launch command in EC2. Most time it should
        //only contain one instance
        for (Instance inst : reservation.getInstances()) {
          if (StringUtils.equals(inst.getInstanceId(), instanceId)) {
            ops.succeed();
            return inst;
          }
        }
      }
    } catch (Exception ex) {
      ops.failed();
      throw ex;
    }
    return null;
  }

  @Override
  public InstanceAttribute getInstanceAttribute(String instanceId, String attributeName) {
    OperationStats ops = new OperationStats("es2InstanceStore", "getInstanceAttribute");
    try {
      awsRateLimiter.acquire();
      DescribeInstanceAttributeRequest request = new DescribeInstanceAttributeRequest()
          .withInstanceId(instanceId)
          .withAttribute(InstanceAttributeName.fromValue(attributeName))
          .withSdkRequestTimeout(300 * 1000).withSdkClientExecutionTimeout(600 * 1000);

      DescribeInstanceAttributeResult result = defaultClient.describeInstanceAttribute(request);
      while (result != null) {
        ops.succeed();
        return result.getInstanceAttribute();
      }
    } catch (Exception ex) {
      ops.failed();
      throw ex;
    }
    return null;
  }

  @Override
  public List<Instance> getInstances(Region region) throws Exception {
    List<Instance> ret = new ArrayList<>();
    List<AvailabilityZone> zones = getAvailabilityZones(region);
    AmazonEC2Client client = getClient(region);
    ExecutorService executor = Executors.newFixedThreadPool(zones.size());
    try {
      List<Callable<List<Instance>>> retrieveFunction = new ArrayList<>();
      for (AvailabilityZone zone : zones) {
        retrieveFunction.add(new Callable<List<Instance>>() {
          @Override
          public List<Instance> call() throws Exception {
            return getInstancesForZone(zone, client);
          }
        });
      }

      List<Future<List<Instance>>> futures = executor.invokeAll(retrieveFunction);
      for (Future<List<Instance>> future : futures) {
        ret.addAll(future.get());
      }

    } finally {
      executor.shutdown();
    }

    return ret;
  }

  @Override
  public List<AvailabilityZone> getAvailabilityZones(Region region) throws Exception {
    OperationStats op = new OperationStats("ec2InstanceStore", "getAvailabilityZones");
    try {
      DescribeAvailabilityZonesRequest request = new DescribeAvailabilityZonesRequest();
      DescribeAvailabilityZonesResult result = getClient(region).describeAvailabilityZones();
      List<AvailabilityZone> ret = result.getAvailabilityZones();
      op.succeed();
      return ret;
    } catch (Exception ex) {
      op.failed();
      throw ex;
    }
  }

  @Override
  public List<Instance> getInstancesForZone(AvailabilityZone zone, AmazonEC2Client client)
      throws Exception {
    OperationStats op = new OperationStats("ec2InstanceStore", "getInstancesForZone");
    try {
      List<Instance> ret = new ArrayList<>();
      DescribeInstancesRequest request = new DescribeInstancesRequest()
          .withMaxResults(1000)
          .withFilters(new Filter("availability-zone", Arrays.asList(zone.getZoneName())))
          .withSdkClientExecutionTimeout(
              600 * 1000) //10 minutes time out for total execution including retries
          .withSdkRequestTimeout(300 * 1000); //5 minutes time out for a single request

      List<Reservation> reservations = new ArrayList<>();
      DescribeInstancesResult result = client.describeInstances(request);
      while (result != null) {
        reservations.addAll(result.getReservations());
        if (result.getNextToken() != null) {
          request.setNextToken(result.getNextToken());
          result = client.describeInstances(request);
        } else {
          result = null;
        }
      }

      for (Reservation reservation : reservations) {
        //Reservation refers to one launch command in EC2. Most time it should
        //only contains one instance
        for (Instance inst : reservation.getInstances()) {
          ret.add(inst);
        }
      }
      op.succeed();
      return ret;
    } catch (Exception ex) {
      op.failed();
      throw ex;
    }
  }

  @Override
  public Map<AvailabilityZone, List<Instance>> getInstancesMapForZone(
      AvailabilityZone zone, AmazonEC2Client client) throws Exception {

    OperationStats op = new OperationStats("ec2InstanceStore", "getInstancesMapForZone");

    try {
      Map<AvailabilityZone, List<Instance>> ret = new HashMap<>();
      ret.put(zone, getInstancesForZone(zone, client));

      op.succeed();
      return ret;

    } catch (Exception e) {

      op.failed();
      logger.error(ExceptionUtils.getRootCauseMessage(e));
      throw e;
    }
  }

  /**
   * Get Reserved instances for a given region
   * @param region
   * @return
   */
  @Override
  public List<ReservedInstances> getReservedInstances(Region region) {

    OperationStats op = new OperationStats("ec2InstanceStore", "getReservedInstances");
    try {

      AmazonEC2Client client = getClient(region);
      DescribeReservedInstancesRequest request = new DescribeReservedInstancesRequest()
          .withSdkClientExecutionTimeout(
              600 * 1000) //10 minutes time out for total execution including retries
          .withSdkRequestTimeout(300 * 1000); //5 minutes time out for a single request

      DescribeReservedInstancesResult result = client.describeReservedInstances(request);

      op.succeed();
      return result.getReservedInstances();

    } catch (Exception e) {

      op.failed();
      logger.error(ExceptionUtils.getRootCauseMessage(e));
      throw e;
    }


  }

  @Override
  public Map<AvailabilityZone, List<ReservedInstances>> getReservedInstancesForZone(
      AvailabilityZone zone, AmazonEC2Client client) throws Exception {

    OperationStats op = new OperationStats("ec2InstanceStore", "getReservedInstancesForZone");

    try {
      Map<AvailabilityZone, List<ReservedInstances>> ret = new HashMap<>();
      DescribeReservedInstancesRequest request = new DescribeReservedInstancesRequest()
          .withFilters(new Filter("availability-zone", Arrays.asList(zone.getZoneName())))
          .withSdkClientExecutionTimeout(
              600 * 1000) //10 minutes time out for total execution including retries
          .withSdkRequestTimeout(300 * 1000); //5 minutes time out for a single request

      DescribeReservedInstancesResult result = client.describeReservedInstances(request);
      ret.put(zone, result.getReservedInstances());

      op.succeed();
      return ret;

    } catch (Exception e) {

      op.failed();
      logger.error(ExceptionUtils.getRootCauseMessage(e));
      throw e;
    }
  }

  /**
   * This function returns a Map of zone to instances running in that aws zone
   *
   * @param region aws region
   * @return Map of zone to instances
   * @throws Exception
   */
  @Override
  public Map<AvailabilityZone, List<Instance>> getInstancesGroupedByZone(Region region)
      throws Exception {
    Map<AvailabilityZone, List<Instance>> ret = new HashMap<>();
    OperationStats op = new OperationStats("ec2InstanceStore", "getInstancesGroupedByZone");
    logger.info("Called getInstancesGroupedByZone");

    try {

      List<AvailabilityZone> zones = getAvailabilityZones(region);
      List<Callable<Map<AvailabilityZone, List<Instance>>>> retrieveFunction = new ArrayList<>();
      AmazonEC2Client client = getClient(region);
      ExecutorService executor = Executors.newFixedThreadPool(zones.size());

      for (AvailabilityZone zone : zones) {
        retrieveFunction.add(new Callable<Map<AvailabilityZone, List<Instance>>>() {
          @Override
          public Map<AvailabilityZone, List<Instance>> call() throws Exception {
            return getInstancesMapForZone(zone, client);
          }
        });
      }

      List<Future<Map<AvailabilityZone, List<Instance>>>> futures =
          executor.invokeAll(retrieveFunction);

      for (Future<Map<AvailabilityZone, List<Instance>>> future : futures) {

        Map<AvailabilityZone, List<Instance>> data = future.get();
        if (data != null) {
          AvailabilityZone zone = data.keySet().iterator().next();
          ret.put(zone, data.get(zone));
        }
      }

      op.succeed();
      return ret;

    } catch (Exception e) {

      op.failed();
      logger.error(ExceptionUtils.getRootCauseMessage(e));
      throw e;
    }
  }


  @Override
  public Map<AvailabilityZone, List<ReservedInstances>> getReservedInstancesGroupedByZone(
      Region region) throws Exception {

    Map<AvailabilityZone, List<ReservedInstances>> ret = new HashMap<>();
    OperationStats op = new OperationStats("ec2InstanceStore",
        "getReservedInstancesGroupedByZone");
    logger.info("Called getReservedInstancesGroupedByZone");

    try {

      List<AvailabilityZone> zones = getAvailabilityZones(region);
      List<Callable<Map<AvailabilityZone, List<ReservedInstances>>>> retrieveFunction
          = new ArrayList<>();

      AmazonEC2Client client = getClient(region);
      ExecutorService executor = Executors.newFixedThreadPool(zones.size());

      for (AvailabilityZone zone : zones) {
        retrieveFunction.add(new Callable<Map<AvailabilityZone, List<ReservedInstances>>>() {
          @Override
          public Map<AvailabilityZone, List<ReservedInstances>> call() throws Exception {
            return getReservedInstancesForZone(zone, client);
          }
        });
      }

      List<Future<Map<AvailabilityZone, List<ReservedInstances>>>> futures =
          executor.invokeAll(retrieveFunction);

      for (Future<Map<AvailabilityZone, List<ReservedInstances>>> future : futures) {

        Map<AvailabilityZone, List<ReservedInstances>> data = future.get();
        if (data != null) {

          AvailabilityZone zone = data.keySet().iterator().next();
          ret.put(zone, data.get(zone));
        }
      }

      op.succeed();
      return ret;

    } catch (Exception e) {

      op.failed();
      logger.error(ExceptionUtils.getRootCauseMessage(e));
      throw e;
    }
  }


  /**
   * This function is a wrapper to the ec2 describeInstanceStatus function.
   *
   * @param region
   * @return List of InstanceStatus
   */
  public List<InstanceStatus> describeInstancesStatusAsync(Region region) throws Exception {

    Preconditions.checkNotNull(region);
    List<InstanceStatus> statusList = new ArrayList<>();
    List<AvailabilityZone> zones = getAvailabilityZones(region);
    AmazonEC2Client client = getClient(region);
    ExecutorService executor = Executors.newFixedThreadPool(zones.size());
    OperationStats op = new OperationStats("ec2InstanceStore", "describeInstancesStatusAsync");
    try {
      List<Callable<List<InstanceStatus>>> retrieveFunction = new ArrayList<>(zones.size());
      for (AvailabilityZone zone : zones) {
        retrieveFunction.add(new Callable<List<InstanceStatus>>() {
          @Override
          public List<InstanceStatus> call() throws Exception {
            return getInstancesStatusByZone(zone, client);
          }
        });
      }

      List<Future<List<InstanceStatus>>> futures = executor.invokeAll(retrieveFunction);
      for (Future<List<InstanceStatus>> future : futures) {
        statusList.addAll(future.get());
      }
      op.succeed();
    } catch (Exception ex) {
      op.failed();
      throw ex;
    } finally {
      executor.shutdown();
    }

    return statusList;
  }

  @Override
  public Map<String, String> getUserData(String instanceId) throws Exception {
    Preconditions.checkNotNull(instanceId);

    awsRateLimiter.acquire();
    OperationStats op = new OperationStats("ec2InstanceStore", "getUserData");
    try {
      InstanceAttribute
          attribute =
          this.getInstanceAttribute(instanceId, "userData");
      if (attribute != null) {
        String base64EncodedString = attribute.getUserData();
        if (StringUtils.isNoneEmpty(base64EncodedString)) {
          Map<String, String> userData = Ec2InstanceStore.parseEc2UserData(base64EncodedString);
          op.succeed();
          return userData;
        }
      }
    } catch (Exception ex) {
      op.failed();
      throw ex;
    }
    op.succeed();
    return new HashMap<>();
  }

  private List<InstanceStatus> getInstancesStatusByZone(AvailabilityZone zone,
                                                        AmazonEC2Client client) {

    // Create this list to capture paginated async results from aws sdk
    List<InstanceStatus> statusList = new ArrayList<>();

    // Create an initial request object
    DescribeInstanceStatusRequest statusRequest = new DescribeInstanceStatusRequest()
        .withMaxResults(1000)
        .withFilters(new Filter("availability-zone", Arrays.asList(zone.getZoneName())))
        .withSdkClientExecutionTimeout(600 * 1000)
        .withSdkRequestTimeout(300 * 1000);

    // Make the request for instanceStatus
    DescribeInstanceStatusResult result = client.describeInstanceStatus(statusRequest);

    // Until more results are available we loop through this code
    while (result != null) {

      statusList.addAll(result.getInstanceStatuses());

      if (result.getNextToken() != null) {

        statusRequest.setNextToken(result.getNextToken());
        result = client.describeInstanceStatus(statusRequest);

      } else {
        result = null;
      }
    }

    // Return all statuses as a list of InstanceStatus objects
    return statusList;
  }

  /**
   * Update tags for one all more instance
   * @param instanceIds
   * @param tags
   * @throws Exception
   */
  @Override
  public void setTagsForInstances(List<String> instanceIds, List<Tag> tags) throws Exception {
    Preconditions.checkNotNull(instanceIds);
    Preconditions.checkNotNull(tags);
    awsRateLimiter.acquire();
    OperationStats op = new OperationStats("ec2InstanceStore", "setTagsForInstances");

    try {
      if (tags.size() > 0) {
        CreateTagsRequest req = new CreateTagsRequest(instanceIds, tags);
        defaultClient.createTags(req);
      }
      op.succeed();
    } catch (Exception ex) {
      op.failed();
      throw ex;
    }
  }
}
