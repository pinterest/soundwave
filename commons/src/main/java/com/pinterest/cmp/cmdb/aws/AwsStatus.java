package com.pinterest.cmp.cmdb.aws;

import com.amazonaws.services.ec2.model.InstanceStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AwsStatus {

  @JsonProperty("raw")
  private InstanceStatus raw;

  @JsonProperty("codes")
  private List<String> codes;

  public InstanceStatus getRaw() {
    return raw;
  }

  public void setRaw(InstanceStatus raw) {
    this.raw = raw;
  }

  public List<String> getCodes() {
    return codes;
  }

  public void setCodes(List<String> codes) {
    this.codes = codes;
  }
}
