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
package com.pinterest.cmp.config;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Wrapper on Configrations
 */
public final class Configuration {

  private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
  private static final Configuration instance = new Configuration();

  private PropertiesConfiguration properties;

  private Configuration() {

    try {
      String file = System.getProperty("config.file", "config/cmdb.dev.properties");
      logger.info("Configuration file is {}", file);
      properties = new PropertiesConfiguration(file);
    } catch (Exception ex) {
      logger.error("Fail to load configuration {}. Error is {}",
          properties == null ? "null" : properties.getFileName(),
          ExceptionUtils.getRootCauseMessage(ex));
    }
  }

  public static PropertiesConfiguration getProperties() {
    return instance.properties;
  }

  public static void setProperties(PropertiesConfiguration configuration) {
    instance.properties = configuration;
  }

}
