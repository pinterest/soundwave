package com.pinterest.soundwave.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Fields {

  @JsonProperty("fields")
  private String fields;

  public String getFields() {
    return fields;
  }

  public void setFields(String fields) {
    this.fields = fields;
  }
}
