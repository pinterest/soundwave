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
