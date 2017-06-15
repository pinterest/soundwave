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

package com.pinterest.soundwave;

import com.pinterest.soundwave.elasticsearch.EsInstanceCounterStore;
import com.pinterest.soundwave.elasticsearch.InstanceCounterStore;
import com.pinterest.soundwave.health.EsHealthChecker;
import com.pinterest.soundwave.pinterest.EsInstanceStore;
import com.pinterest.soundwave.pinterest.EsServiceMappingStore;
import com.pinterest.soundwave.pinterest.ServiceMappingStore;
import com.pinterest.soundwave.resources.DailySnapshot;
import com.pinterest.soundwave.resources.Health;
import com.pinterest.soundwave.resources.Instance;
import com.pinterest.soundwave.resources.InstanceCounter;
import com.pinterest.soundwave.resources.Query;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.glassfish.jersey.filter.LoggingFilter;


public class ApiApplication extends Application<ApiConfiguration> {

  public static void main(final String[] args) throws Exception {
    new ApiApplication().run(args);
  }

  @Override
  public String getName() {
    return "api";
  }

  @Override
  public void initialize(final Bootstrap<ApiConfiguration> bootstrap) {
    // TODO: application initialization
    bootstrap.addBundle(new SwaggerBundle<ApiConfiguration>() {
      @Override
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
          ApiConfiguration configuration) {
        SwaggerBundleConfiguration config = new SwaggerBundleConfiguration();
        config.setResourcePackage("com.pinterest.soundwave");
        return config;
      }
    });
  }

  @Override
  public void run(final ApiConfiguration configuration,
                  final Environment environment) {

    // Common initializations
    EsInstanceStore cmdbInstanceStore = new EsInstanceStore();
    ServiceMappingStore serviceMappingStore = new EsServiceMappingStore();
    InstanceCounterStore instanceCounterStore = new EsInstanceCounterStore();

    // Health checks
    final EsHealthChecker esHealthChecker = new EsHealthChecker(cmdbInstanceStore);
    environment.healthChecks().register("cmdbStore", esHealthChecker);

    // Logging inbound request/response
    environment.jersey().register(
        new LoggingFilter(java.util.logging.Logger.getLogger("InboundRequestResponse"), true));

    // Add API endpoints here
    final Instance instance = new Instance(cmdbInstanceStore);
    environment.jersey().register(instance);

    final Health health = new Health();
    environment.jersey().register(health);

    final Query query = new Query(cmdbInstanceStore);
    environment.jersey().register(query);

    final InstanceCounter instanceCounter = new InstanceCounter(instanceCounterStore);
    environment.jersey().register(instanceCounter);

    final DailySnapshot dailySnapshot = new DailySnapshot();
    environment.jersey().register(dailySnapshot);
  }

}
