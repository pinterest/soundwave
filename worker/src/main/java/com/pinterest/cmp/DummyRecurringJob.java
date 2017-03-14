package com.pinterest.cmp;

import java.util.Random;
import java.util.concurrent.Callable;

public class DummyRecurringJob implements Callable<Boolean> {

  private static Random random = new Random(System.currentTimeMillis());
  private int failRate;
  private int executionSeconds;

  public DummyRecurringJob(int failRate, int executionSeconds) {
    this.failRate = failRate;
    this.executionSeconds = executionSeconds;
  }

  @Override
  public Boolean call() throws Exception {
    Thread.sleep(executionSeconds * 1000);
    if (failRate >= 100 - random.nextInt(100)) {
      //Either throw or return false
      if (random.nextBoolean()) {
        throw new Exception("Got an exception");
      } else {
        return false;
      }
    }
    return true;

  }
}
