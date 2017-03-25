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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MessageProcessingResult {

  public static final String EVENTCREATEDFIELD = "EventCreatedTime";
  private String instanceId;
  private String messageType;
  private long duration;
  private boolean succeed;
  private MessageProcessingError error;
  private Date timestamp;
  private Map<String, Object> info;
  private long toSqsTime;

  public MessageProcessingResult(NotificationEvent event) {
    this.setInfo(new HashMap<>());
    this.setMessageType(event.getDetail().getState());
    this.setInstanceId(event.getDetail().getInstanceId());
    this.putEventCreatedTime(event.getTime());
    this.timestamp = DateTime.now(DateTimeZone.UTC).toDate();
    this.error = MessageProcessingError.NOERROR;

  }

  public MessageProcessingError getError() {
    return error;
  }

  public void setError(MessageProcessingError error) {
    this.error = error;
    setSucceed(error == MessageProcessingError.NOERROR);
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }

  public Map<String, Object> getInfo() {
    return info;
  }

  public void setInfo(Map<String, Object> info) {
    this.info = info;
  }

  public boolean isSucceed() {
    return succeed;
  }

  public void setSucceed(boolean succeed) {
    this.succeed = succeed;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public void putEventCreatedTime(Date date) {
    this.getInfo().put(EVENTCREATEDFIELD, date);
  }

  public long getToSqsTime() {
    return toSqsTime;
  }

  public void setToSqsTime(long toSqsTime) {
    this.toSqsTime = toSqsTime;
  }

  public enum MessageProcessingError {
    NOERROR,
    NO_NAME_TAG,
    NOT_EXIST_IN_EC_2,
    NOT_EXIST_IN_ES,
    QUOBLE_NAME_NOT_AVAILABLE,
    STILL_PENDING,
  }
}
