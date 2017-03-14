package com.pinterest.cmp.cmdb.pinterest;

import com.pinterest.cmp.cmdb.bean.EsDocument;
import com.pinterest.cmp.cmdb.bean.EsInstanceConfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * A bean class represent a instance and its tags
 */
public class EsInstanceTags implements EsDocument {

  @JsonProperty("service_mapping")
  private String[] serviceMappings;

  @JsonProperty("svc_tag")
  private String[] serviceTag;

  @JsonProperty("sys_tag")
  private String[] sysTag;

  @JsonProperty("usage_tag")
  private String[] usageTag;

  @JsonProperty("id")
  private String id;

  @JsonProperty("config")
  private EsInstanceConfig config;

  @JsonProperty("updated_time")
  private Date updatedTime;

  @JsonProperty("created_time")
  private Date createdTime;

  @JsonIgnore
  private long version;

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public EsInstanceConfig getConfig() {
    return config;
  }

  public void setConfig(EsInstanceConfig config) {
    this.config = config;
  }

  public Date getUpdatedTime() {
    return updatedTime;
  }

  public void setUpdatedTime(Date updatedTime) {
    this.updatedTime = updatedTime;
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


  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }


  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }
}
