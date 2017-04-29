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
package com.pinterest.soundwave.pinterest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The class for the customize tagging
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EsServiceMapping {

  private static final Logger logger = LoggerFactory.getLogger(EsServiceMapping.class);

  @JsonIgnore
  List<Pattern> patterns = new ArrayList<>();

  @JsonProperty("name")
  private String name;

  @JsonProperty("svc_tag")
  private String serviceTag;

  @JsonProperty("sys_tag")
  private String sysTag;

  @JsonProperty("usage_tag")
  private String usageTag;

  @JsonProperty("value")
  private String value;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getServiceTag() {
    return serviceTag;
  }

  public void setServiceTag(String serviceTag) {
    this.serviceTag = serviceTag;
  }

  public String getSysTag() {
    return sysTag;
  }

  public void setSysTag(String sysTag) {
    this.sysTag = sysTag;
  }

  public String getUsageTag() {
    return usageTag;
  }

  public void setUsageTag(String usageTag) {
    this.usageTag = usageTag;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void buildMatchPatterns() {
    if (StringUtils.isNotEmpty(this.getValue())) {
      patterns.clear();
      String[] expressions = this.getValue().split(",");
      for (String expression : expressions) {
        if (StringUtils.isNotEmpty(expression)) {
          String formalizedPattern = StringUtils.strip(StringUtils.strip(expression, " "), "/");
          patterns.add(Pattern.compile(formalizedPattern));
        }
      }
    }
  }

  public boolean matches(String input) {
    boolean ret = false;
    for (Pattern pattern : this.patterns) {
      if (pattern.matcher(input).matches()) {
        logger.debug("Match {} for input {}", pattern, input);
        ret = true;
        break;
      }
    }
    return ret;
  }

}
