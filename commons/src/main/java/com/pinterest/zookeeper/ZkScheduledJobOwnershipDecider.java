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
import com.pinterest.job.OwnershipDecider;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.UUID;

/**
 * An ownership decider based on Zookeeper leader election.
 */
public class ZkScheduledJobOwnershipDecider implements OwnershipDecider {

  private static final Logger
      logger =
      LoggerFactory.getLogger(ZkScheduledJobOwnershipDecider.class);
  private static String nodeIdentifier;

  static {
    try {
      nodeIdentifier =
          InetAddress.getLocalHost().getHostName() + "_" + UUID.randomUUID().toString();
    } catch (Exception e) {
      logger.warn("Cannot get the node name");
      nodeIdentifier = "unknown" + UUID.randomUUID().toString();
    }
  }

  private String zkPath;
  private LeaderLatch latch;


  private ZkScheduledJobOwnershipDecider(String zkPath) {
    Preconditions.checkArgument(StringUtils.isNotEmpty(zkPath));
    this.zkPath = zkPath;
  }

  public static ZkScheduledJobOwnershipDecider buildDecider(String identifier) throws Exception {
    ZkScheduledJobOwnershipDecider decider = new ZkScheduledJobOwnershipDecider(
        String.format("%s/job/%s/owner",
            StringUtils.stripEnd(Configuration.getProperties().getString("zk_path"), "/"),
            identifier));
    decider.latch =
        new LeaderLatch(ZkClient.getClient(), decider.zkPath, nodeIdentifier,
            LeaderLatch.CloseMode.NOTIFY_LEADER);
    decider.latch.start();
    return decider;
  }

  @Override
  public boolean isOwner() {
    return latch.hasLeadership();
  }

  @Override
  public String getNodeName() {
    return nodeIdentifier;
  }

}
