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
package com.pinterest.zookeeper;

import com.pinterest.config.Configuration;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * A singleton instance for accessing zk
 */
public class ZkClient {

  private static CuratorFramework client = null;

  private ZkClient() {}

  public static CuratorFramework getClient() {
    if (client == null) {
      synchronized (ZkClient.class) {
        if (client == null) {
          client = CuratorFrameworkFactory
              .newClient(Configuration.getProperties().getString("zk_connection_string"),
                  new ExponentialBackoffRetry(1000, 10000000)); //TODO. Make it read from config
          client.start();
        }
      }
    }
    return client;
  }
}
