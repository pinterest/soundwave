package com.pinterest.cmp.cmdb.bean;

import com.pinterest.cmp.cmdb.aws.AwsStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bean represents the AwsStatus of an instance in Es
 */
public class EsAwsStatus implements EsDocument {

  @JsonProperty("id")
  private String id;

  @JsonProperty("aws_status")
  private AwsStatus awsStatus;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @JsonIgnore
  private long version;

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public AwsStatus getAwsStatus() {
    return awsStatus;
  }

  public void setAwsStatus(AwsStatus awsStatus) {
    this.awsStatus = awsStatus;
  }

}
