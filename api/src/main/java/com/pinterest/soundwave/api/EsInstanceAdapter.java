package com.pinterest.soundwave.api;

import com.pinterest.soundwave.annotations.StringDate;
import com.pinterest.soundwave.aws.AwsStatus;
import com.pinterest.soundwave.bean.EsInstanceConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EsInstanceAdapter {

  @JsonProperty("service_mapping")
  private String[] serviceMappings;

  @JsonProperty("svc_tag")
  private String[] serviceTag;

  @JsonProperty("sys_tag")
  private String[] sysTag;

  @JsonProperty("usage_tag")
  private String[] usageTag;

  @JsonProperty("id")
  private String id;

  @JsonProperty("region")
  private String region;

  @JsonProperty("location")
  private String location;

  @JsonProperty("nodepool")
  private String nodePool;

  @JsonProperty("state")
  private String state;

  @JsonProperty("deployment")
  private String deployment;

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

  @JsonProperty("config")
  private EsInstanceConfig config;

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

  @JsonProperty("facts")
  private Map<String, Object> facts;

  @JsonProperty("pkgs")
  private Map<String, Object> pkgs;

  @JsonProperty("token")
  private String token;

  @JsonProperty("cached")
  private int cached;

  @JsonProperty("defunct_count")
  private int defunctCount;

  @JsonProperty("pc")
  private int pc;

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

  public EsInstanceConfig getConfig() {
    return config;
  }

  public void setConfig(EsInstanceConfig config) {
    this.config = config;
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

  public String[] getServiceMappings() {
    return serviceMappings;
  }

  public void setServiceMappings(String[] serviceMappings) {
    this.serviceMappings = serviceMappings;
  }

  public String[] getServiceTag() {
    return serviceTag;
  }

  public void setServiceTag(String[] serviceTag) {
    this.serviceTag = serviceTag;
  }

  public String[] getSysTag() {
    return sysTag;
  }

  public void setSysTag(String[] sysTag) {
    this.sysTag = sysTag;
  }

  public String[] getUsageTag() {
    return usageTag;
  }

  public void setUsageTag(String[] usageTag) {
    this.usageTag = usageTag;
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

  public Map<String, Object> getFacts() {
    return facts;
  }

  public void setFacts(Map<String, Object> facts) {
    this.facts = facts;
  }

  public Map<String, Object> getPkgs() {
    return pkgs;
  }

  public void setPkgs(Map<String, Object> pkgs) {
    this.pkgs = pkgs;
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

  public int getDefunctCount() {
    return defunctCount;
  }

  public void setDefunctCount(int defunctCount) {
    this.defunctCount = defunctCount;
  }

  public int getPc() {
    return pc;
  }

  public void setPc(int pc) {
    this.pc = pc;
  }
}
