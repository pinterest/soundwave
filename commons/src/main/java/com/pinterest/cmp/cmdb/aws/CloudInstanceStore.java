package com.pinterest.cmp.cmdb.aws;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.InstanceAttribute;
import com.amazonaws.services.ec2.model.ReservedInstances;

import java.util.List;
import java.util.Map;

public interface CloudInstanceStore {

  Instance getInstance(String instanceId) throws Exception;

  InstanceAttribute getInstanceAttribute(String instanceId, String attributeName) throws Exception;

  List<Instance> getInstances(Region region) throws Exception;

  Map<String,String> getUserData(String instanceId) throws Exception;

  List<AvailabilityZone> getAvailabilityZones(Region region) throws Exception;

  List<Instance> getInstancesForZone(AvailabilityZone zone, AmazonEC2Client client)
      throws Exception;

  Map<AvailabilityZone, List<Instance>> getInstancesGroupedByZone(Region region) throws Exception;

  Map<AvailabilityZone, List<Instance>> getInstancesMapForZone(
      AvailabilityZone zone, AmazonEC2Client client) throws Exception;

  Map<AvailabilityZone, List<ReservedInstances>>  getReservedInstancesGroupedByZone(Region region)
      throws Exception;

  Map<AvailabilityZone, List<ReservedInstances>> getReservedInstancesForZone(
      AvailabilityZone zone, AmazonEC2Client client) throws Exception;

  void setTagsForInstances(List<String> instanceIds, List<Tag> tags) throws Exception;

  List<ReservedInstances> getReservedInstances(Region region);
}
