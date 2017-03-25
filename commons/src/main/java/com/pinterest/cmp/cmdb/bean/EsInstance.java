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
package com.pinterest.cmp.cmdb.bean;

import com.pinterest.cmp.cmdb.aws.AwsStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The ElasticSearch instance of an EC2 instance
 */
public class EsInstance implements EsDocument {

  private static final Map<String, List<String>> STATE_TRANSITION_MAP =
      ImmutableMap.<String, List<String>>builder().put(State.RUNNING.toString(),
          Arrays.asList(State.STOPPED.toString(), State.TERMINATED.toString()))
          .put(State.STOPPED.toString(),
              Arrays.asList(State.RUNNING.toString(), State.TERMINATED.toString()))
          .put(State.SHUTTINGDOWN.toString(),
              Arrays.asList(State.TERMINATED.toString()))
          .put(State.PENDING.toString(),
              Arrays.asList(State.RUNNING.toString(), State.TERMINATED.toString()))
          .put(State.DEFUNCT.toString(),
              Arrays.asList(State.TERMINATED.toString())).build();

  @JsonProperty("id")
  private String id;

  @JsonProperty("region")
  private String region;

  @JsonProperty("location")
  private String location;

  @JsonProperty("state")
  private String state;

  @JsonProperty("created_time")
  private Date createdTime;

  @JsonProperty("updated_time")
  private Date updatedTime;

  @JsonProperty("terminated_time")
  private Date terminateTime;

  @JsonProperty("vpc_id")
  private String vpcId;

  @JsonProperty("subnet_id")
  private String subnetId;

  @JsonProperty("aws_launch_time")
  private Date awsLaunchTime;

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

  @JsonIgnore
  private long version;

  public int getCached() {
    return cached;
  }

  public void setCached(int cached) {
    this.cached = cached;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

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


  public String getState() {
    return state;
  }

  public void setState(String state) throws InvalidStateTransitionException {
    if (StringUtils.isEmpty(this.state)) {
      this.state = state;
    } else if (!StringUtils.equals(this.state, state)) {
      if (STATE_TRANSITION_MAP.containsKey(this.state) && STATE_TRANSITION_MAP.get(this.state)
          .contains(state)) {
        this.state = state;
      } else {
        throw new InvalidStateTransitionException(
            String.format("Invalid transit state from %s to %s", this.state, state));
      }
    }
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
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

  public Date getTerminateTime() {
    return terminateTime;
  }

  public void setTerminateTime(Date terminateTime) {
    this.terminateTime = terminateTime;
  }

  public Date getUpdatedTime() {
    return updatedTime;
  }

  public void setUpdatedTime(Date updatedTime) {
    this.updatedTime = updatedTime;
  }

  public Date getAwsLaunchTime() {
    return awsLaunchTime;
  }

  public void setAwsLaunchTime(Date awsLaunchTime) {
    this.awsLaunchTime = awsLaunchTime;
  }

  public AwsStatus getAwsStatus() {
    return awsStatus;
  }

  public void setAwsStatus(AwsStatus awsStatus) {
    this.awsStatus = awsStatus;
  }




}
