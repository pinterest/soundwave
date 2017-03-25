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
package com.pinterest.cmp.cmdb.pinterest;


import com.pinterest.cmp.cmdb.aws.AwsStatus;
import com.pinterest.cmp.cmdb.bean.EsDocument;
import com.pinterest.cmp.cmdb.bean.EsInstanceConfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EsMetaData implements EsDocument {

  @JsonProperty("id")
  private String id;

  @JsonProperty("region")
  private String region;

  @JsonProperty("location")
  private String location;

  @JsonProperty("nodepool")
  private String nodePool;

  @JsonProperty("deployment")
  private String deployment;

  @JsonProperty("subnet_id")
  private String subnetId;

  @JsonProperty("config")
  private EsInstanceConfig config;

  @JsonProperty("security_groups")
  private List<String> securityGroups;

  @JsonProperty("cloud")
  private Map<String, Object> cloud = new HashMap<>();

  @JsonProperty("service_mapping")
  private String[] serviceMappings;

  @JsonProperty("usage_tag")
  private String[] usageTag;

  @JsonProperty("vpc_id")
  private String vpcId;

  @JsonProperty("aws_launch_time")
  private Date awsLaunchTime;

  @JsonProperty("aws_status")
  private AwsStatus awsStatus;

  @JsonIgnore
  private long version;

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  /* Getters and setters */

  public AwsStatus getAwsStatus() {
    return awsStatus;
  }

  public void setAwsStatus(AwsStatus awsStatus) {
    this.awsStatus = awsStatus;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getNodePool() {
    return nodePool;
  }

  public void setNodePool(String nodePool) {
    this.nodePool = nodePool;
  }

  public String getDeployment() {
    return deployment;
  }

  public void setDeployment(String deployment) {
    this.deployment = deployment;
  }

  public String getSubnetId() {
    return subnetId;
  }

  public void setSubnetId(String subnetId) {
    this.subnetId = subnetId;
  }

  public EsInstanceConfig getConfig() {
    return config;
  }

  public void setConfig(EsInstanceConfig config) {
    this.config = config;
  }

  public List<String> getSecurityGroups() {
    return securityGroups;
  }

  public void setSecurityGroups(List<String> securityGroups) {
    this.securityGroups = securityGroups;
  }

  public Map<String, Object> getCloud() {
    return cloud;
  }

  public void setCloud(Map<String, Object> cloud) {
    this.cloud = cloud;
  }

  public String[] getServiceMappings() {
    return serviceMappings;
  }

  public void setServiceMappings(String[] serviceMappings) {
    this.serviceMappings = serviceMappings;
  }

  public String[] getUsageTag() {
    return usageTag;
  }

  public void setUsageTag(String[] usageTag) {
    this.usageTag = usageTag;
  }

  public String getVpcId() {
    return vpcId;
  }

  public void setVpcId(String vpcId) {
    this.vpcId = vpcId;
  }

  public Date getAwsLaunchTime() {
    return awsLaunchTime;
  }

  public void setAwsLaunchTime(Date awsLaunchTime) {
    this.awsLaunchTime = awsLaunchTime;
  }


}
