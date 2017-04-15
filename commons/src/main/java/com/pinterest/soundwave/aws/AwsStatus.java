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

import com.amazonaws.services.ec2.model.InstanceStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AwsStatus {

  @JsonProperty("raw")
  private InstanceStatus raw;

  @JsonProperty("codes")
  private List<String> codes;

  public InstanceStatus getRaw() {
    return raw;
  }

  public void setRaw(InstanceStatus raw) {
    this.raw = raw;
  }

  public List<String> getCodes() {
    return codes;
  }

  public void setCodes(List<String> codes) {
    this.codes = codes;
  }
}
