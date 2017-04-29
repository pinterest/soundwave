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
package com.pinterest.soundwave.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class PinterestEsInstance extends EsInstance {

  @JsonProperty("service_mapping")
  private String[] serviceMappings;

  @JsonProperty("svc_tag")
  private String[] serviceTag;

  @JsonProperty("sys_tag")
  private String[] sysTag;

  @JsonProperty("usage_tag")
  private String[] usageTag;

  @JsonProperty("nodepool")
  private String nodePool;

  @JsonProperty("deployment")
  private String deployment;


  @JsonProperty("facts")
  private Map<String, Object> facts;

  @JsonProperty("pkgs")
  private Map<String, Object> pkgs;

  @JsonProperty("config")
  private EsInstanceConfig config;


  @JsonProperty("defunct_count")
  private int defunctCount;

  @JsonProperty("pc")
  private int pc;

  public String getNodePool() {
    return nodePool;
  }

  public void setNodePool(String nodePool) {
    this.nodePool = nodePool;
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


  public EsInstanceConfig getConfig() {
    return config;
  }

  public void setConfig(EsInstanceConfig config) {
    this.config = config;
  }


  public String[] getServiceMappings() {
    return serviceMappings;
  }

  public void setServiceMappings(String[] serviceMappings) {
    this.serviceMappings = serviceMappings;
  }

  public String[] getServiceTag() {
    return serviceTag;
  }

  public void setServiceTag(String[] serviceTag) {
    this.serviceTag = serviceTag;
  }

  public String[] getSysTag() {
    return sysTag;
  }

  public void setSysTag(String[] sysTag) {
    this.sysTag = sysTag;
  }

  public String[] getUsageTag() {
    return usageTag;
  }

  public void setUsageTag(String[] usageTag) {
    this.usageTag = usageTag;
  }

  public String getDeployment() {
    return deployment;
  }

  public void setDeployment(String deployment) {
    this.deployment = deployment;
  }

  public int getDefunctCount() {
    return defunctCount;
  }

  public void setDefunctCount(int defunctCount) {
    this.defunctCount = defunctCount;
  }

  public int getPc() {
    return pc;
  }

  public void setPc(int pc) {
    this.pc = pc;
  }

}
