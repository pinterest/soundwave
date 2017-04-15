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
package com.pinterest.soundwave.aws;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The detail of the instance notification
 */
public class Ec2InstanceNotificationDetail {

  @JsonProperty("state")
  private String state;

  @JsonProperty("instance-id")
  private String instanceId;


  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }


}
