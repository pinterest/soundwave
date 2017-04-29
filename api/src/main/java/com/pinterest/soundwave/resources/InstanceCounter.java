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

import com.pinterest.soundwave.utils.Utils;
import com.pinterest.OperationStats;
import com.pinterest.soundwave.bean.EsInstanceCountRecord;
import com.pinterest.soundwave.elasticsearch.InstanceCounterStore;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
public class InstanceCounter {

  private static final Logger logger = LoggerFactory.getLogger(InstanceCounter.class);
  private InstanceCounterStore instanceCounterStore;
  private final DateFormat dateFormat;

  public InstanceCounter(InstanceCounterStore instanceCounterStore) {
    this.instanceCounterStore = instanceCounterStore;
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  }

  @GET
  @Path("/instancetyperecords/daily/{date}")
  public Response getInstanceTypeRecordsDaily(@PathParam("date") @NotNull String dateStr) {

    OperationStats opStats = new OperationStats("cmdb_api", "get_instance_type_records_daily",
        new HashMap<>());
    Map<String, String> tags = new HashMap<>();

    try {

      List<EsInstanceCountRecord> recordList = new ArrayList<>();

      Date date = dateFormat.parse(dateStr);
      logger.info("Instance Counts requested for Date {}", date.toString());

      Iterator<EsInstanceCountRecord> recordsIterator =
              instanceCounterStore.getCountRecordsByDate(date);

      while (recordsIterator.hasNext()) {
        recordList.add(recordsIterator.next());
      }

      // Metrics
      tags.put("status", String.valueOf(Response.Status.OK.getStatusCode()));
      opStats.succeed(tags);

      return Response.status(Response.Status.OK)
          .type(MediaType.APPLICATION_JSON)
          .entity(recordList)
          .build();

    } catch (ParseException e) {

      logger.error(ExceptionUtils.getRootCauseMessage(e));
      logger.error(ExceptionUtils.getFullStackTrace(e));

      tags.put("status", String.valueOf(Response.Status.BAD_REQUEST.getStatusCode()));
      tags.put("message", e.getClass().getSimpleName());
      opStats.succeed(tags);

      return Response.status(Response.Status.BAD_REQUEST)
          .type(MediaType.APPLICATION_JSON)
          .build();

    } catch (Exception e) {

      return Utils.responseException(e, logger, opStats, tags);
    }
  }
}
