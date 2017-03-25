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
package com.pinterest.cmp.cmdb.aws;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 *  The event message definition from the SQS notification
 */
public class NotificationEvent {

  @JsonProperty("account")
  private String account;

  @JsonProperty("region")
  private String region;

  @JsonProperty("detail")
  private Ec2InstanceNotificationDetail detail;

  @JsonProperty("detail-type")
  private String detailType;

  @JsonProperty("source")
  private String source;

  @JsonProperty("version")
  private long version;

  @JsonProperty("time")
  private Date time;

  @JsonProperty("id")
  private String id;

  @JsonProperty("resources")
  private String[] resources;

  @JsonIgnore
  private Message sourceMessage;

  private Date sqsSentTime;

  public String getDetailType() {
    return detailType;
  }

  public void setDetailType(String detailType) {
    this.detailType = detailType;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String[] getResources() {
    return resources;
  }

  public void setResources(String[] resources) {
    this.resources = resources;
  }

  public Date getSqsSentTime() {
    return sqsSentTime;
  }

  public void setSqsSentTime(Date sqsSentTime) {
    this.sqsSentTime = sqsSentTime;
  }

  public Message getSourceMessage() {
    return sourceMessage;
  }

  public void setSourceMessage(Message sourceMessage) {
    this.sourceMessage = sourceMessage;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public Ec2InstanceNotificationDetail getDetail() {
    return detail;
  }

  public void setDetail(Ec2InstanceNotificationDetail detail) {
    this.detail = detail;
  }
}
