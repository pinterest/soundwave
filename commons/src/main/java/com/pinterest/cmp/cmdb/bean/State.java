package com.pinterest.cmp.cmdb.bean;

import org.apache.commons.lang.StringUtils;

public enum State {
  RUNNING("running"),
  TERMINATED("terminated"),
  SHUTTINGDOWN("shutting-down"),
  STOPPED("stopped"),
  PENDING("pending"),
  DEFUNCT("defunct");

  private final String text;

  private State(final String text) {
    this.text = text;
  }

  public boolean isState(String val) {
    return StringUtils.equals(val, this.text);
  }

  @Override
  public String toString() {
    return text;
  }

}
