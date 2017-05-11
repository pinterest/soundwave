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

package com.pinterest.soundwave.api;

import com.pinterest.soundwave.annotations.StringDate;
import com.pinterest.soundwave.aws.AwsStatus;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EsInstanceAdapter {


  @JsonProperty("id")
  private String id;

  @JsonProperty("region")
  private String region;

  @JsonProperty("location")
  private String location;

  @JsonProperty("state")
  private String state;

  @JsonProperty("created_time")
  @StringDate
  private String createdTime;

  @JsonProperty("updated_time")
  @StringDate
  private String updatedTime;

  @JsonProperty("terminated_time")
  @StringDate
  private String terminateTime;

  @JsonProperty("vpc_id")
  private String vpcId;

  @JsonProperty("subnet_id")
  private String subnetId;

  @JsonProperty("aws_launch_time")
  @StringDate
  private String awsLaunchTime;

  @JsonProperty("security_groups")
  private List<String> securityGroups;

  @JsonProperty("security_group_ids")
  private List<String> securityGroupIds;

  @JsonProperty("tags")
  private Map<String, String> tags;

  @JsonProperty("cloud")
  private Map<String, Object> cloud = new HashMap<>();

  @JsonProperty("aws_status")
  private AwsStatus awsStatus;

  @JsonProperty("token")
  private String token;

  @JsonProperty("cached")
  private int cached;

  public EsInstanceAdapter() {}

  public List<String> getSecurityGroups() {
    return securityGroups;
  }

  public void setSecurityGroups(List<String> securityGroups) {
    this.securityGroups = securityGroups;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }

  public List<String> getSecurityGroupIds() {
    return securityGroupIds;
  }

  public void setSecurityGroupIds(List<String> securityGroupIds) {
    this.securityGroupIds = securityGroupIds;
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

  public String getVpcId() {
    return vpcId;
  }

  public void setVpcId(String vpcId) {
    this.vpcId = vpcId;
  }

  public String getSubnetId() {
    return subnetId;
  }

  public void setSubnetId(String subnetId) {
    this.subnetId = subnetId;
  }

  public Map<String, Object> getCloud() {
    return cloud;
  }

  public void setCloud(Map<String, Object> cloud) {
    this.cloud = cloud;
  }

  public AwsStatus getAwsStatus() {
    return awsStatus;
  }

  public void setAwsStatus(AwsStatus awsStatus) {
    this.awsStatus = awsStatus;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  public String getUpdatedTime() {
    return updatedTime;
  }

  public void setUpdatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
  }

  public String getTerminateTime() {
    return terminateTime;
  }

  public void setTerminateTime(String terminateTime) {
    this.terminateTime = terminateTime;
  }

  public String getAwsLaunchTime() {
    return awsLaunchTime;
  }

  public void setAwsLaunchTime(String awsLaunchTime) {
    this.awsLaunchTime = awsLaunchTime;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public int getCached() {
    return cached;
  }

  public void setCached(int cached) {
    this.cached = cached;
  }
}
