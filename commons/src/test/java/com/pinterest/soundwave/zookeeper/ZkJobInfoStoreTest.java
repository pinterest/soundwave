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
package com.pinterest.soundwave.zookeeper;

import com.pinterest.job.JobRunInfo;
import com.pinterest.zookeeper.ZkJobInfoStore;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ZkJobInfoStoreTest {

  @Test
  @Ignore
  public void jobInfoStoreTests() throws Exception {
    ZkJobInfoStore store = new ZkJobInfoStore();
    JobRunInfo run = new JobRunInfo();
    run.setSucceed(true);
    run.setJobName("TestJob");
    run.setStartTime(DateTime.now().minusHours(1).toDate());
    store.updateLatestRun(run);
    JobRunInfo saved = store.getLatestRun(run.getJobName());
    Assert.assertTrue(run.isSucceed());
    Assert.assertEquals(saved.getStartTime(), run.getStartTime());

  }

  @Test
  @Ignore
  public void jobConfigTests() throws Exception {
    ZkJobInfoStore store = new ZkJobInfoStore();
    boolean b = store.isJobDisabled("UpdateCloudHealth");
  }

}
