package com.pinterest.cmp.cmdb.aws;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The detail of the instance notification
 */
public class Ec2InstanceNotificationDetail {

  @JsonProperty("state")
  private String state;

  @JsonProperty("instance-id")
  private String instanceId;


  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }


}
