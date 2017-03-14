package com.pinterest.cmp.job;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class JobConfig {

  @JsonProperty("isDisabled")
  private boolean isDisabled;


  public boolean isDisabled() {
    return isDisabled;
  }

  public void setDisabled(boolean disabled) {
    isDisabled = disabled;
  }
}
