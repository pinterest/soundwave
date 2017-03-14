package com.pinterest.cmp.cmdb.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;


public class EsQueryResult extends HashMap implements EsDocument {

  @JsonProperty("id")
  private String id;

  @JsonIgnore
  private long version;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public long getVersion() {
    return version;
  }

  @Override
  public void setVersion(long version) {
    this.version = version;
  }


}
