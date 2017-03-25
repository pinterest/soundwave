/**
 * Copyright 2017 Pinterest, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pinterest.cmp.cmdb.pinterest;


import com.pinterest.cmp.cmdb.bean.EsDocument;
import com.pinterest.cmp.cmdb.bean.EsInstanceConfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class EsNameMetaData implements EsDocument {

  @JsonProperty("nodepool")
  private String nodePool;

  @JsonProperty("id")
  private String id;

  @JsonProperty("security_groups")
  private List<String> securityGroups;

  @JsonProperty("location")
  private String location;

  @JsonProperty("region")
  private String region;

  @JsonProperty("deployment")
  private String deployment;

  @JsonProperty("config")
  private EsInstanceConfig config;

  @JsonIgnore
  private long version;

  public String getNodePool() {
    return nodePool;
  }

  public void setNodePool(String nodePool) {
    this.nodePool = nodePool;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public List<String> getSecurityGroups() {
    return securityGroups;
  }

  public void setSecurityGroups(List<String> securityGroups) {
    this.securityGroups = securityGroups;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getDeployment() {
    return deployment;
  }

  public void setDeployment(String deployment) {
    this.deployment = deployment;
  }

  public EsInstanceConfig getConfig() {
    return config;
  }

  public void setConfig(EsInstanceConfig config) {
    this.config = config;
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
