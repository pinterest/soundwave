package com.pinterest.cmp.cmdb.bean;

import org.apache.commons.lang.StringUtils;

public enum EsStatus {
  SUCCESS("success"),
  TIMEOUT("timeout"),
  ERROR("error"),
  UNKNOWN("unknown");

  private final String text;

  EsStatus(final String text) {
    this.text = text;
  }

  public boolean isStatus(String val) {
    return StringUtils.equals(val, this.text);
  }

  @Override
  public String toString() {
    return text;
  }

}