package com.pinterest.soundwave.resources;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class DailySnapshotTest {

  @Test
  @Ignore
  public void getDailySnapshot() throws Exception {
    DailySnapshot snapshot = new DailySnapshot();
    Response resp = snapshot.getDailySnapshot("2016-12-12");
    Assert.assertNotNull(resp);
  }

}
