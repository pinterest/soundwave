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
package com.pinterest.soundwave;


import com.pinterest.JobManager;
import com.pinterest.soundwave.aws.Ec2InstanceStore;
import com.pinterest.soundwave.pinterest.EsInstanceStore;
import com.pinterest.zookeeper.ZkJobInfoStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main method that initializes logger, service framework proxy, finagle server and ostrich
 */
public class WorkerMain {

  private static final Logger LOG =
      LoggerFactory.getLogger(WorkerMain.class);
  private static final Logger FAILURE_LOG = LoggerFactory.getLogger("failure");
  private static final Logger SLOW_LOG = LoggerFactory.getLogger("slow");

  private static final String MAX_CONNECTION_IDLE_CONFIG_KEY = "max_connection_idle_in_minute";
  private static final String MAX_CONCURRENT_REQUESTS_KEY = "max_concurrent_requests";
  private static final String OSTRICH_PORT_KEY = "ostrich_port";
  private static final String SERVICE_NAME_KEY = "service_name";
  private static final String CLUSTER_NAME_KEY = "cluster_name";
  private static final String THRIFT_PORT_KEY = "thrift_server_port";
  private static final String SERVER_SET_PATH_KEY = "server_set_path";
  private static final String SLOW_REQUEST_THRESHOLD_MILLIS = "slow_request_threshold_millis";

  public static void main(String[] args) {
    try {
      if (System.getProperty("config.file") == null) {
        System.setProperty("config.file", "soundwave.opensource.properties");
      }
      //Start the Job Manager
      JobManager
          manager =
          new JobManager(new ZkJobInfoStore(), new Ec2InstanceStore(), new EsInstanceStore());
      manager.start();

      while (true) {
        Thread.sleep(1000);
      }

      //CHECKSTYLE_OFF: IllegalCatch
    } catch (Exception e) {
      LOG.error("Cannot start the service properly", e);
      System.exit(1);
    }
    //CHECKSTYLE_ON: IllegalCatch
  }
}
