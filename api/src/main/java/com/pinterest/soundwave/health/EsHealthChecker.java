package com.pinterest.soundwave.health;

import com.pinterest.soundwave.bean.EsStatus;
import com.pinterest.soundwave.elasticsearch.CmdbInstanceStore;

import com.codahale.metrics.health.HealthCheck;

public class EsHealthChecker extends HealthCheck implements DBHealthChecker {

  private final CmdbInstanceStore cmdbInstanceStore;

  public EsHealthChecker(CmdbInstanceStore cmdbInstanceStore) {
    this.cmdbInstanceStore = cmdbInstanceStore;
  }

  @Override
  public Result check() throws Exception {

    String status = cmdbInstanceStore.checkStatus();

    if (EsStatus.SUCCESS.isStatus(status)) {
      return Result.healthy();

    } else if (EsStatus.ERROR.isStatus(status)) {
      return Result.unhealthy("Es Status is red");

    } else if (EsStatus.TIMEOUT.isStatus(status)) {
      return Result.unhealthy("timeout");

    } else {
      return Result.unhealthy("unknown issue");
    }
  }
}
