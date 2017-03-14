package com.pinterest.cmp.cmdb.pinterest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class EsFactsAndPackages {

  @JsonProperty("id")
  private String id;

  @JsonProperty("facts")
  private Map<String, Object> facts = new HashMap<>();

  @JsonProperty("pkgs")
  private Map<String, Object> pkgs = new HashMap<>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, Object> getFacts() {
    return facts;
  }

  public void setFacts(Map<String, Object> facts) {
    this.facts = facts;
  }

  public Map<String, Object> getPkgs() {
    return pkgs;
  }

  public void setPkgs(Map<String, Object> pkgs) {
    this.pkgs = pkgs;
  }
}
