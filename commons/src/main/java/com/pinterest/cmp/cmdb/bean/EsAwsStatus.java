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

import com.pinterest.cmp.cmdb.aws.AwsStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bean represents the AwsStatus of an instance in Es
 */
public class EsAwsStatus implements EsDocument {

  @JsonProperty("id")
  private String id;

  @JsonProperty("aws_status")
  private AwsStatus awsStatus;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @JsonIgnore
  private long version;

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public AwsStatus getAwsStatus() {
    return awsStatus;
  }

  public void setAwsStatus(AwsStatus awsStatus) {
    this.awsStatus = awsStatus;
  }

}
