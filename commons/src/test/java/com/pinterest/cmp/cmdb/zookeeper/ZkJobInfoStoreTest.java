package com.pinterest.cmp.cmdb.zookeeper;

import com.pinterest.cmp.job.JobRunInfo;
import com.pinterest.cmp.zookeeper.ZkJobInfoStore;

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
