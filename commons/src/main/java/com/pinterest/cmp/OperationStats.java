package com.pinterest.cmp;

import com.twitter.ostrich.stats.Stats;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Map;

public final class OperationStats {

  private String methodName;
  private String statsName;
  private StopWatch watch;
  private Map<String, String> tags;

  public OperationStats(String methodName, String statsName) {
    this(methodName, statsName, null);
  }

  public OperationStats(String methodName, String statsName, Map<String, String> tags) {
    this.methodName = methodName;
    this.statsName = statsName;
    this.tags = tags;
    Stats.incr(StatsUtil.getStatsName(methodName, statsName, tags));
    watch = new StopWatch();
    watch.start();
  }


  public void succeed() {
    this.watch.stop();
    Stats
        .incr(StatsUtil
            .getStatsName(this.methodName, this.statsName, StatsUtil.StatsType.SUCCESS, tags));
    Stats.addMetric(
        StatsUtil.getStatsName(this.methodName, this.statsName, StatsUtil.StatsType.TIME, tags),
        (int) watch.getTime());
  }

  public void succeed(Map<String, String> additionalTags) {

    this.watch.stop();

    if (tags == null) {
      this.tags = additionalTags;
    } else {
      this.tags.putAll(additionalTags);
    }

    Stats
        .incr(StatsUtil
            .getStatsName(this.methodName, this.statsName, StatsUtil.StatsType.SUCCESS, tags));
    Stats.addMetric(
        StatsUtil.getStatsName(this.methodName, this.statsName, StatsUtil.StatsType.TIME, tags),
        (int) watch.getTime());
  }

  public void failed() {
    this.watch.stop();
    Stats
        .incr(StatsUtil
            .getStatsName(this.methodName, this.statsName, StatsUtil.StatsType.FAILURE, tags));
    Stats.addMetric(
        StatsUtil.getStatsName(this.methodName, this.statsName, StatsUtil.StatsType.TIME, tags),
        (int) watch.getTime());
  }

  public void failed(Map<String, String> additionalTags) {

    this.watch.stop();

    if (tags == null) {
      this.tags = additionalTags;
    } else {
      this.tags.putAll(additionalTags);
    }

    Stats
        .incr(StatsUtil
            .getStatsName(this.methodName, this.statsName, StatsUtil.StatsType.FAILURE, tags));
    Stats.addMetric(
        StatsUtil.getStatsName(this.methodName, this.statsName, StatsUtil.StatsType.TIME, tags),
        (int) watch.getTime());
  }

  public long getTime() {
    return this.watch.getTime();
  }
}
