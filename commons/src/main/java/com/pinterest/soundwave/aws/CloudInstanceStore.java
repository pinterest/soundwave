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
