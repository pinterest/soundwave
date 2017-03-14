package com.pinterest.cmp.job;

import com.pinterest.cmp.config.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A job executor for scheduled recurring jobs that guarantee runs on only a single node
 */
public class ExclusiveRecurringJobExecutor implements JobExecutor {

  private static final Logger logger = LoggerFactory.getLogger(ExclusiveRecurringJobExecutor.class);
  private static final Logger jobLogger = LoggerFactory.getLogger("scheduledJobLog");
  private static final Random random = new Random(System.currentTimeMillis());
  private static final ScheduledExecutorService execService = Executors.newScheduledThreadPool(
      Configuration.getProperties().getInt("num_of_scheduled_runner", 16));
  private static ObjectMapper mapper = new ObjectMapper()
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  private long interval;
  private Callable<Boolean> jobFunction;
  private OwnershipDecider ownershipDecider;
  private String identifier;
  private JobInfoStore jobInfoStore;

  public ExclusiveRecurringJobExecutor(JobInfoStore jobInfoStore, long interval,
                                       Callable<Boolean> job, String identifier,
                                       OwnershipDecider ownershipDecider) {
    Preconditions.checkNotNull(jobInfoStore);
    Preconditions.checkArgument(StringUtils.isNotEmpty(identifier));
    Preconditions.checkNotNull(job);
    Preconditions.checkNotNull(ownershipDecider);
    Preconditions.checkArgument(interval > 0);
    this.setInterval(interval);
    this.setJobFunction(job);
    this.setOwnershipDecider(ownershipDecider);
    this.setIdentifier(identifier);
    this.jobInfoStore = jobInfoStore;

  }

  public static ScheduledExecutorService getExecService() {
    return execService;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public OwnershipDecider getOwnershipDecider() {
    return ownershipDecider;
  }

  public void setOwnershipDecider(OwnershipDecider ownershipDecider) {
    this.ownershipDecider = ownershipDecider;
  }

  public long getInterval() {
    return interval;
  }

  public void setInterval(long interval) {
    this.interval = interval;
  }

  public Callable<Boolean> getJobFunction() {
    return jobFunction;
  }

  public void setJobFunction(Callable<Boolean> jobFunction) {
    this.jobFunction = jobFunction;
  }


  @Override
  public void execute() {
    execService.scheduleAtFixedRate(() -> run(),
        this.getDelay(),
        this.getInterval(), TimeUnit.SECONDS);
  }

  public void run() {
    try {

      JobRunInfo info = new JobRunInfo();
      info.setStartTime(DateTime.now(DateTimeZone.UTC).toDate());
      info.setJobName(identifier);
      info.setNode(ownershipDecider.getNodeName());
      info.setRetryCount(0);

      if (jobInfoStore.isJobDisabled(identifier)) {
        logger.info("{} has been disabled. Skip the run", identifier);
        return;
      }
      int retryCount = Configuration.getProperties().getInt("job_retry_count", 3);
      while (!info.isSucceed() && retryCount-- > 0) {
        try {
          if (this.getOwnershipDecider().isOwner()) {
            jobInfoStore.updateLatestRun(info);
            logger.info("Running job {}", this.getIdentifier());
            boolean result = this.getJobFunction().call();
            info.setSucceed(result);
          } else {
            logger.info("Not the owner of {}. Skip it", this.getIdentifier());
            return;
          }
        } catch (Throwable ex) {
          String error = ExceptionUtils.getRootCauseMessage(ex);
          logger.error(error);
          logger.error(ExceptionUtils.getFullStackTrace(ex));
          info.setRetryCount(info.getRetryCount() + 1);
          info.setError(error);
        }
      }
      info.setEndTime(DateTime.now(DateTimeZone.UTC).toDate());
      jobLogger.info(mapper.writeValueAsString(info));
      jobInfoStore.updateLatestRun(info);
    } catch (Exception ex) {
      logger.error(ExceptionUtils.getRootCauseMessage(ex));
      logger.error(ExceptionUtils.getFullStackTrace(ex));
    }
  }


  /**
   * Decide when the job should start run in first time
   * @return Seconds for the Job to start
   */
  public int getDelay() {
    try {
      JobRunInfo lastRun = jobInfoStore.getLatestRun(this.identifier);

      if (lastRun != null && lastRun.isSucceed()) {
        Period
            period =
            new Period(new DateTime(lastRun.getStartTime()), DateTime.now(DateTimeZone.UTC));
        if (period.toStandardSeconds().getSeconds() < this.interval) {
          return (int) (this.interval - period.toStandardSeconds().getSeconds());
        }
      }
    } catch (Exception ex) {
      logger.error(ExceptionUtils.getRootCauseMessage(ex));
      logger.error(ExceptionUtils.getFullStackTrace(ex));
    }

    return random.nextInt(Configuration.getProperties().getInt("job_random_delay", 60));
  }
}
