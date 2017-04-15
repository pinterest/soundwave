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
import com.pinterest.job.JobConfig;
import com.pinterest.job.JobInfoStore;
import com.pinterest.job.JobRunInfo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkJobInfoStore extends JobInfoStore {

  private static final Logger logger = LoggerFactory.getLogger(ZkJobInfoStore.class);

  private static ObjectMapper mapper = new ObjectMapper()
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(
          DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
      );

  private static String
      zkPath =
      StringUtils.stripEnd(Configuration.getProperties().getString("zk_path"), "/");

  @Override
  public JobRunInfo getLatestRun(String jobType) throws Exception {
    String path = String.format("%s/job/%s/latestrun", zkPath,
        jobType);
    ensureZkPathExists(path);
    byte[] data = ZkClient.getClient().getData().forPath(path);
    if (data != null && data.length > 0) {
      try {
        return mapper.readValue(IOUtils.toString(data, "UTF-8"), JobRunInfo.class);
      } catch (Exception e) {
        logger.error("Fail to read last run. Error {}", ExceptionUtils.getRootCauseMessage(e));
        return null;
      }
    } else {
      return null;
    }
  }

  @Override
  public void updateLatestRun(JobRunInfo runInfo) throws Exception {
    String path = String.format("%s/job/%s/latestrun", zkPath,
        runInfo.getJobName());
    ensureZkPathExists(path);
    ZkClient.getClient().setData().forPath(path, mapper.writeValueAsBytes(runInfo));
  }

  @Override
  public boolean isJobDisabled(String jobType) throws Exception {
    String path = String.format("%s/job/%s", zkPath,
        jobType);

    if (ZkClient.getClient().checkExists().forPath(path) != null) {
      byte[] content = ZkClient.getClient().getData().forPath(path);
      if (content != null && content.length > 0) {
        JobConfig config = mapper.readValue(IOUtils.toString(content, "UTF-8"), JobConfig.class);
        return config.isDisabled();
      }
    }

    return false;
  }

  private void ensureZkPathExists(String path) throws Exception {
    if (ZkClient.getClient().checkExists().forPath(path) == null) {
      try {
        ZkClient.getClient().create().creatingParentsIfNeeded().forPath(path);
      } catch (KeeperException.NodeExistsException ex) {
        //Okay to ignore. It means another node may just create it between our
        //check and create.
      }
    }
  }
}
