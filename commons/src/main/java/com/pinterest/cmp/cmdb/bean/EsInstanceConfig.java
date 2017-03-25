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
package com.pinterest.cmp.cmdb.bean;

import com.pinterest.cmp.cmdb.utils.JsonCompareUtil;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 {"architecture":"x86_64","image_id":"ami-19097f7c","instance_lifecycle":"fixed",
 "instance_type":"i2.2xlarge","internal_address":"10.1.29.19","internal_dns":"ip-10-1-29-19.ec2
 .internal","kernel_id":null,
 "name":"pinalyticsv2-a01-regionserver-00039ea0",
 "pexternal_dns":"pinalyticsv2-a01-regionserver-00039ea0.use1.pinadmin.com",
 "pexternal_domain":"use1.pinadmin.com","pinternal_dns":"pinalyticsv2-a01-regionserver-00039ea0
 .use1.pin220.com",
 "pinternal_domain":"use1.pin220.com"}

 */
public class EsInstanceConfig {

  @JsonProperty("architecture")
  private String architecture;

  @JsonProperty("image_id")
  private String imageId;

  @JsonProperty("image_app_name")
  private String imageAppName;
  @JsonProperty("instance_lifecycle")
  private String instanceLifeCycle;
  @JsonProperty("instance_type")
  private String instanceType;
  @JsonProperty("internal_address")
  private String internalAddress;
  @JsonProperty("external_address")
  private String externalAddress;
  @JsonProperty("internal_dns")
  private String internalDns;
  @JsonProperty("external_dns")
  private String externalDns;
  @JsonProperty("kernel_id")
  private String kernelId;
  @JsonProperty("name")
  private String name;
  @JsonProperty("pexternal_dns")
  private String pexternalDns;
  @JsonProperty("pexternal_domain")
  private String pexternalDomain;
  @JsonProperty("pinternal_dns")
  private String pinternalDns;
  @JsonProperty("pinternal_domain")
  private String pinternalDomain;

  public String getImageAppName() {
    return imageAppName;
  }

  public void setImageAppName(String imageAppName) {
    this.imageAppName = imageAppName;
  }

  public String getPexternalDomain() {
    return pexternalDomain;
  }

  public void setPexternalDomain(String pexternalDomain) {
    this.pexternalDomain = pexternalDomain;
  }

  public String getPinternalDns() {
    return pinternalDns;
  }

  public void setPinternalDns(String pinternalDns) {
    this.pinternalDns = pinternalDns;
  }

  public String getExternalDns() {
    return externalDns;
  }

  public void setExternalDns(String externalDns) {
    this.externalDns = externalDns;
  }

  public String getArchitecture() {
    return architecture;
  }

  public void setArchitecture(String architecture) {
    this.architecture = architecture;
  }

  public String getExternalAddress() {
    return externalAddress;
  }

  public void setExternalAddress(String externalAddress) {
    this.externalAddress = externalAddress;
  }

  public String getImageId() {
    return imageId;
  }

  public void setImageId(String imageId) {
    this.imageId = imageId;
  }

  public String getInstanceLifeCycle() {
    return instanceLifeCycle;
  }

  public void setInstanceLifeCycle(String instanceLifeCycle) {
    this.instanceLifeCycle = instanceLifeCycle;
  }

  public String getInstanceType() {
    return instanceType;
  }

  public void setInstanceType(String instanceType) {
    this.instanceType = instanceType;
  }

  public String getInternalAddress() {
    return internalAddress;
  }

  public void setInternalAddress(String internalAddress) {
    this.internalAddress = internalAddress;
  }

  public String getInternalDns() {
    return internalDns;
  }

  public void setInternalDns(String internalDns) {
    this.internalDns = internalDns;
  }

  public String getKernelId() {
    return kernelId;
  }

  public void setKernelId(String kernelId) {
    this.kernelId = kernelId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPexternalDns() {
    return pexternalDns;
  }

  public void setPexternalDns(String pexternalDns) {
    this.pexternalDns = pexternalDns;
  }

  public String getPinternalDomain() {
    return pinternalDomain;
  }

  public void setPinternalDomain(String pinternalDomain) {
    this.pinternalDomain = pinternalDomain;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof EsInstanceConfig)) {
      return false;
    }
    boolean ret = false;
    try {
      ret = JsonCompareUtil.findDiff(this, (EsInstanceConfig) obj).size() == 0;
    } catch (Exception ex) {

    }
    return ret;
  }


}
