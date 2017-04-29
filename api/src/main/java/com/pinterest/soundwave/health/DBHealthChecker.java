package com.pinterest.soundwave.health;

import com.codahale.metrics.health.HealthCheck;

public interface DBHealthChecker {
  HealthCheck.Result check() throws Exception;
}
