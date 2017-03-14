package com.pinterest.cmp.zookeeper;

import com.pinterest.cmp.config.Configuration;

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
