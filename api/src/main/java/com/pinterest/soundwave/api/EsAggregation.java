package com.pinterest.soundwave.api;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Accepts a comma seperated list of strings
 */
public class EsAggregation {

  @JsonProperty("query")
  private String query;

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }
}
