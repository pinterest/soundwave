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
