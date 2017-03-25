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
package com.pinterest.cmp.job;

import com.pinterest.cmp.config.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *  A Job exector that runs function in a while loop
 */
public class ForeverRepeatedJobExecutor implements JobExecutor {

  private static final Logger logger = LoggerFactory.getLogger(ForeverRepeatedJobExecutor.class);
  private static final ScheduledExecutorService execService = Executors.newScheduledThreadPool(
      Configuration.getProperties().getInt("num_subscriber", 1));
  private long interval;
  private Runnable runnable;

  public ForeverRepeatedJobExecutor(long interval, Runnable runnable) {
    this.setInterval(interval);
    this.setRunnable(runnable);
  }

  public long getInterval() {
    return interval;
  }

  protected void setInterval(long interval) {
    this.interval = interval;
  }

  public Runnable getRunnable() {
    return runnable;
  }

  protected void setRunnable(Runnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public void execute() {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.submit(() -> {
      execService.scheduleAtFixedRate(getRunnable(), 0, getInterval(), TimeUnit.MILLISECONDS);
    });
  }

  protected void runOnce() {
    this.getRunnable().run();
  }

}
