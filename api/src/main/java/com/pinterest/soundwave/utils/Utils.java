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
package com.pinterest.soundwave.utils;


import com.pinterest.OperationStats;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;

import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Utils {

  public static Response responseException(Exception e, Logger logger,
                                           OperationStats opStats, Map<String, String> tags) {

    logger.error(ExceptionUtils.getFullStackTrace(e));
    logger.error(ExceptionUtils.getRootCauseMessage(e));

    tags.put("status", String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    tags.put("message", e.getClass().getSimpleName());

    opStats.failed(tags);

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .type(MediaType.APPLICATION_JSON)
        .build();
  }
}
