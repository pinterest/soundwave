package com.pinterest.cmp.job;

/**
 * Abstract interfaces to retrieve the latest job run info. This is main used
 * to decide the right scheduled time
 */
public abstract class JobInfoStore {

  public abstract JobRunInfo getLatestRun(String jobType) throws Exception;

  public abstract void updateLatestRun(JobRunInfo info) throws Exception;

  public abstract boolean isJobDisabled(String jobType) throws Exception;
}
