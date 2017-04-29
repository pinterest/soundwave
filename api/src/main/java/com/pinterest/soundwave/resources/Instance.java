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
package com.pinterest.soundwave.resources;

import com.pinterest.soundwave.api.EsInstanceAdapter;
import com.pinterest.soundwave.utils.ObjectAdapter;
import com.pinterest.soundwave.utils.Utils;
import com.pinterest.OperationStats;
import com.pinterest.soundwave.aws.AwsStatus;
import com.pinterest.soundwave.bean.EsAwsStatus;
import com.pinterest.soundwave.bean.EsInstance;
import com.pinterest.soundwave.elasticsearch.CmdbInstanceStore;

import com.amazonaws.services.ec2.model.InstanceStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v2/")
@Produces(MediaType.APPLICATION_JSON)
public class Instance {

  private static final Logger logger = LoggerFactory.getLogger(Instance.class);
  private CmdbInstanceStore cmdbInstanceStore;

  public Instance(CmdbInstanceStore cmdbInstanceStore) {
    this.cmdbInstanceStore = cmdbInstanceStore;
  }

  @GET
  @Path("/instance/{instanceId}")
  public Response getInstance(@PathParam("instanceId") @NotNull String instanceId) {

    OperationStats opStats = new OperationStats("cmdb_api","get_instance", new HashMap<>());
    Map<String, String> tags = new HashMap<>();

    try {

      // Get instance from cmdbStore
      EsInstance instance = cmdbInstanceStore.getInstanceById(instanceId);

      if (instance == null) {

        // Metrics tags
        tags.put("status", String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));
        opStats.succeed(tags);

        logger.info("Instance not found in cmdbStore {}", instanceId);
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .build();
      }

      // Metrics
      tags.put("status", String.valueOf(Response.Status.OK.getStatusCode()));
      opStats.succeed(tags);

      logger.info("Found instance in cmdbStore {}", instanceId);

      EsInstanceAdapter esInstanceAdapter =
              ObjectAdapter.getObject(instance, EsInstanceAdapter.class);
      return Response.status(Response.Status.OK)
          .type(MediaType.APPLICATION_JSON)
          .entity(esInstanceAdapter)
          .build();

    } catch (Exception e) {
      return Utils.responseException(e, logger, opStats, tags);
    }

  }

  @GET
  @Path("/instances/status/{instanceId}")
  public Response getInstancesStatus(@PathParam("instanceId") @NotNull String instanceId) {

    OperationStats opStats =
            new OperationStats("cmdb_api", "get_instances_status", new HashMap<>());
    Map<String, String> tags = new HashMap<>();

    try {

      List<String> instanceIds = Arrays.asList(instanceId);

      // Get response of statuses
      Response statuses = getAwsInstancesStatus(instanceIds);

      // Metrics tags
      tags.put("status", String.valueOf(Response.Status.OK.getStatusCode()));
      opStats.succeed(tags);

      return statuses;

    } catch (Exception e) {
      return Utils.responseException(e, logger, opStats, tags);
    }
  }

  private Response getAwsInstancesStatus(List<String> instanceIds) throws Exception {

    Map<String, Integer> statuses = new HashMap<>();
    for (String instanceId : instanceIds) {

      int intStatus = 0;
      EsAwsStatus status = cmdbInstanceStore.getAwsStatus(instanceId);

      if (status == null) {

        // Instance Status not found in cmdbStore . Mark status as -1
        logger.info("No status found for instanceId {}", instanceId);
        intStatus = -1;

      } else {

        AwsStatus awsStatus = status.getAwsStatus();
        if (awsStatus != null) {

          List<String> codes = awsStatus.getCodes();
          InstanceStatus raw = awsStatus.getRaw();
          String instanceStatus = raw.getInstanceStatus().getStatus();
          String systemStatus = raw.getSystemStatus().getStatus();

          if (codes != null) {

            if (StringUtils.equalsIgnoreCase(instanceStatus, "initializing")
                    || StringUtils.equalsIgnoreCase(systemStatus, "initializing")) {

              // codes are defined but one of the status is initializing
              logger.info("Aws Status requested. Instance is still initializing...");
              intStatus = 0;

            } else {

              // codes are present and system or instance status is abnormal
              logger.warn("Unhealthy instance was found. {} with status {} and {}",
                  instanceId, instanceStatus, systemStatus);
              intStatus = 1;
            }
          }

        }
      }

      // Add trimmed status of the instance to statuses Map
      statuses.put(instanceId, intStatus);

    }

    return Response.status(Response.Status.OK)
        .type(MediaType.APPLICATION_JSON)
        .entity(statuses)
        .build();
  }

}
