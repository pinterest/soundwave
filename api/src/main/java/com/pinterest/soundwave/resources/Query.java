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


import com.pinterest.soundwave.api.EsAggregation;
import com.pinterest.soundwave.api.EsQuery;
import com.pinterest.soundwave.utils.Utils;
import com.pinterest.OperationStats;
import com.pinterest.soundwave.bean.EsQueryResult;
import com.pinterest.soundwave.elasticsearch.CmdbInstanceStore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v2/")
@Produces(MediaType.APPLICATION_JSON)
public class Query {

  private static final Logger logger = LoggerFactory.getLogger(Query.class);
  private CmdbInstanceStore cmdbInstanceStore;
  private static ObjectMapper mapper = new ObjectMapper();
  private static final List<String> dateFieldNames = new ArrayList<>(Arrays.asList(
      "terminated_time", "launch_time",
      "launchTime", "aws_launch_time",
      "created_time", "updated_time"));

  public Query(CmdbInstanceStore cmdbInstanceStore) {

    this.cmdbInstanceStore = cmdbInstanceStore;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/query")
  public Response query(EsQuery esQuery) {

    OperationStats opStats = new OperationStats("cmdb_api", "query", new HashMap<>());
    Map<String, String> tags = new HashMap<>();

    String queryString = esQuery.getQueryString();
    String fields = esQuery.getFields();

    try {

      // Metrics tags
      tags.put("status", String.valueOf(Response.Status.OK.getStatusCode()));
      opStats.succeed(tags);

      logger.info("Success: query - {}", queryString);

      String[] includeFields = StringUtils.split(fields, ",");
      Iterator<EsQueryResult> output = cmdbInstanceStore.query(queryString, includeFields);
      // Process iterator items one by one instead of converting to list
      List<Map<String, Object>> results = generateFlatOutput(output, includeFields);
      return Response.status(Response.Status.OK)
          .type(MediaType.APPLICATION_JSON)
          .entity(results)
          .build();

    } catch (Exception e) {
      return Utils.responseException(e, logger, opStats, tags);

    }

    // End of query function
  }

  /**
   * This function processes curl request made without using a json header
   *
   * @param text String format of the data
   * @return Response object
   */
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("/query")
  public Response textQuery(String text) {

    try {
      String input = URLDecoder.decode(text, "UTF-8");
      EsQuery esQuery = mapper.readValue(input, EsQuery.class);

      return query(esQuery);

    } catch (Exception e) {

      OperationStats opStats = new OperationStats("cmdb_api", "query", new HashMap<>());
      Map<String, String> tags = new HashMap<>();

      logger.error("Error in processing the input string. Cannot map to esQuery.class");
      return Utils.responseException(e, logger, opStats, tags);

    }

    // End of text query function
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/aggregations/terms")
  public Response getAggregations(EsAggregation esAggregation) {

    Preconditions.checkNotNull(esAggregation);

    OperationStats opStats = new OperationStats("cmdb_api", "get_aggregations", new HashMap<>());
    Map<String, String> tags = new HashMap<>();
    String query = esAggregation.getQuery();

    try {

      String[] aggregationParams = StringUtils.split(query, ",");

      if ( aggregationParams.length == 0 ) {

        logger.warn("Aggregation Request with no fields");
        tags.put("status", String.valueOf(Response.Status.BAD_REQUEST.getStatusCode()));
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .build();
      }

      // Metrics tags
      tags.put("status", String.valueOf(Response.Status.OK.getStatusCode()));
      opStats.succeed(tags);

      logger.info("Success: aggregation - {}", query);

      List<String> aggregationParamsList = Arrays.asList(aggregationParams);
      Map<String, HashMap> results = cmdbInstanceStore.getAggregations(aggregationParamsList);
      return Response.status(Response.Status.OK)
          .type(MediaType.APPLICATION_JSON)
          .entity(results)
          .build();

    } catch (Exception e) {

      return Utils.responseException(e, logger, opStats, tags);
    }
  }

  @GET
  @Path("/aggregations/terms/{queryString}")
  public Response getAggregationsUrl(@PathParam("queryString") @NotNull String query) {

    EsAggregation aggregation = new EsAggregation();
    aggregation.setQuery(query);

    return getAggregations(aggregation);
  }

  List<Map<String, Object>> generateFlatOutput(Iterator<EsQueryResult> esQueryResults,
                                               String[] includeFields) {

    List<Map<String, Object>> flatResults = new ArrayList<>();
    while (esQueryResults.hasNext()) {

      EsQueryResult esQueryResult = esQueryResults.next();

      Map<String, Object> data = new HashMap<>();

      // For each user requested field
      for (String fieldName : includeFields) {

        // Hierarchy
        if (StringUtils.contains(fieldName, ".")) {
          Object value = processHierarchy(fieldName, esQueryResult);

          if (dateFieldNames.contains(fieldName)) {

            if (value != null) {

              DateTime dateTime = new DateTime(value, DateTimeZone.UTC);
              data.put(fieldName, dateTime.toString());

            } else {
              // If any date is null set it to an empty string
              data.put(fieldName, "");
            }

          } else {
            data.put(fieldName, value);

          }

        } else {
          // Simple field

          if (esQueryResult.containsKey(fieldName)) {
            // If key is present

            Object value = esQueryResult.get(fieldName);

            if (dateFieldNames.contains(fieldName)) {

              if (value != null) {

                DateTime dateTime = new DateTime(value, DateTimeZone.UTC);
                data.put(fieldName, dateTime.toString());

              } else {
                // If any date is null set it to an empty string
                data.put(fieldName, "");
              }

            } else {
              data.put(fieldName, value);

            }

          } else {
            // Field name not in response
            data.put(fieldName, "");
          }
        }
      }

      flatResults.add(data);
    }

    return flatResults;
  }

  Object processHierarchy(String fieldName, EsQueryResult esQueryResult) {

    Object outputValue = "";
    String[] propertyNames = StringUtils.split(fieldName, ".");
    Map<String, Object> inputData = esQueryResult;

    for (int i = 0; i < propertyNames.length; i++) {

      if (inputData.containsKey(propertyNames[i])) {
        // input data contains the key
        if (i == (propertyNames.length - 1)) {
          // Last in hierarchy
          outputValue = inputData.get(propertyNames[i]);
          break;

        } else {

          inputData = (Map<String, Object>) inputData.get(propertyNames[i]);
        }
      } else {

        outputValue = "";
        break;
      }
    }

    return outputValue;
  }

  // End of class
}
