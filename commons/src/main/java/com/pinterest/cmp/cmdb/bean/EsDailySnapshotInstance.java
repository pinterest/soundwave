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

import com.pinterest.cmp.cmdb.pinterest.EsInstanceStore;
import com.pinterest.cmp.cmdb.utils.JsonCompareUtil;
import com.pinterest.cmp.cmdb.utils.StringArrayOrElementStringDeserializer;
import com.pinterest.cmp.cmdb.utils.StringListOrElementDeserializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The daily snapshot document (See build_dss_data_from_instance)
 */
public class EsDailySnapshotInstance implements EsDocument {

  @JsonProperty("id")
  private String id;


  @JsonIgnore
  private long version;


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

  @EsStoreMappingProperty(store = EsInstanceStore.class, value = "aws_launch_time")
  @JsonProperty("launch_time")
  private Date launchTime;

  /**
   * Create customized serializer. Currently in cmdb_ss index, the document is not consistent.
   * If it is created from SQS, the value is a array. If it is from the Job it is a string.
   * Write customized serializer to handle both cases
   */
  @JsonProperty("service_mapping")
  @JsonDeserialize(using = StringArrayOrElementStringDeserializer.class)
  private String[] serviceMappings;

  @JsonProperty("svc_tag")
  @JsonDeserialize(using = StringArrayOrElementStringDeserializer.class)
  private String[] serviceTag;

  @JsonProperty("sys_tag")
  @JsonDeserialize(using = StringArrayOrElementStringDeserializer.class)
  private String[] sysTag;

  @JsonProperty("usage_tag")
  @JsonDeserialize(using = StringArrayOrElementStringDeserializer.class)
  private String[] usageTag;

  @JsonProperty("security_groups")
  @JsonDeserialize(using = StringListOrElementDeserializer.class)
  private List<String> securityGroups;

  @EsStoreMappingProperty(store = EsInstanceStore.class, value = "config.instance_lifecycle")
  @JsonProperty("lifecycle")
  private String lifecycle;

  @EsStoreMappingProperty(store = EsInstanceStore.class, value = "config.instance_type")
  @JsonProperty("type")
  private String type;

  @EsStoreMappingProperty(store = EsInstanceStore.class, value = "config.name")
  @JsonProperty("name")
  private String name;

  @JsonProperty("terminated_time")
  private Date terminateTime;

  @EsStoreMappingProperty(store = EsInstanceStore.class, ignore = true)
  @JsonProperty("run_time")
  private String runTime;

  public EsDailySnapshotInstance() {

  }

  @JsonCreator
  public EsDailySnapshotInstance(@JsonProperty("config") EsInstanceConfig config) {
    if (config != null) {
      this.setLifecycle(config.getInstanceLifeCycle());
      this.setName(config.getName());
      this.setType(config.getInstanceType());
    }
  }


  public EsDailySnapshotInstance(PinterestEsInstance esInstance) {
    Preconditions.checkNotNull(esInstance);
    Preconditions.checkNotNull(esInstance.getConfig());

    this.setId(esInstance.getId());
    this.setDeployment(esInstance.getDeployment());
    this.setNodePool(esInstance.getNodePool());
    this.setState(esInstance.getState());
    this.setRegion(esInstance.getRegion());
    this.setLocation(esInstance.getLocation());
    this.setSecurityGroups(esInstance.getSecurityGroups());
    this.setServiceMappings(esInstance.getServiceMappings());
    this.setServiceTag(esInstance.getServiceTag());
    this.setSysTag(esInstance.getSysTag());
    this.setUsageTag(esInstance.getUsageTag());
    this.setTerminateTime(roundToSeconds(esInstance.getTerminateTime()));
    this.setLaunchTime(roundToSeconds(esInstance.getAwsLaunchTime())); //Round milliseconds

    this.setLifecycle(esInstance.getConfig().getInstanceLifeCycle());
    this.setType(esInstance.getConfig().getInstanceType());
    this.setName(esInstance.getConfig().getName());

    if (esInstance.getState() == State.TERMINATED.toString() && this.getTerminateTime() != new Date(
        0)) {
      this.setRunTime(getStringOfPeriod(this.getLaunchTime(), this.getTerminateTime()));
    }
  }

  public static final Date roundToSeconds(Date dt) {
    if (dt != null) {
      return new Date(dt.getTime() / 1000 * 1000);
    }
    return dt;
  }

  /**
   * Convert the timer from begin to end to a string representation
   * like 1D 2M 3H 4m 5s
   * @param begin
   * @param end
   * @return The String representation
   */
  public static final String getStringOfPeriod(Date begin, Date end) {
    Preconditions.checkArgument(begin.getTime() < end.getTime());

    Period period = new Period(new DateTime(begin), new DateTime(end));
    StringBuilder strBuilder = new StringBuilder();

    appendFieldIfGreat(strBuilder, "Y", period.getYears());
    appendFieldIfGreat(strBuilder, "M", period.getMonths());
    appendFieldIfGreat(strBuilder, "D", period.getWeeks() * 7 + period.getDays());
    appendFieldIfGreat(strBuilder, "h", period.getHours());
    appendFieldIfGreat(strBuilder, "m", period.getMinutes());
    appendFieldIfGreat(strBuilder, "s", period.getSeconds());

    if (strBuilder.length() > 0) {
      strBuilder.setLength(strBuilder.length() - 1); //Remove last space
    }
    return strBuilder.toString();
  }

  private static final void appendFieldIfGreat(StringBuilder strBuilder, String appendSuffix,
                                               int value) {
    if (value > 0) {
      strBuilder.append(String.format("%s%s ", value, appendSuffix));
    }
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

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getDeployment() {
    return deployment;
  }

  public void setDeployment(String deployment) {
    this.deployment = deployment;
  }

  public Date getLaunchTime() {
    return launchTime;
  }

  public void setLaunchTime(Date launchTime) {
    this.launchTime = launchTime;
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

  public List<String> getSecurityGroups() {
    return securityGroups;
  }

  public void setSecurityGroups(List<String> securityGroups) {
    this.securityGroups = securityGroups;
  }

  public String getLifecycle() {
    return lifecycle;
  }

  public void setLifecycle(String lifecycle) {
    this.lifecycle = lifecycle;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getTerminateTime() {
    return terminateTime;
  }

  public void setTerminateTime(Date terminateTime) {
    this.terminateTime = terminateTime;
  }

  public String getRunTime() {
    return runTime;
  }

  public void setRunTime(String runTime) {
    this.runTime = runTime;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  @JsonProperty("config")
  public void setConfig(Map<String, Object> foo) {
    this.setLifecycle((String) foo.get("instanceLifeCycle"));
  }

  /**
   * Compare with another instance and returns a diff
   * @param inst
   * @return
   */
  public Map<String, Object[]> findDiff(EsDailySnapshotInstance inst) throws Exception {
    return JsonCompareUtil.findDiff(this, inst);
  }
}
