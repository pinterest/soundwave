package com.pinterest.soundwave.resources;


import com.pinterest.soundwave.utils.Utils;
import com.pinterest.OperationStats;
import com.pinterest.soundwave.aws.DailySnapshotStore;
import com.pinterest.soundwave.aws.DailySnapshotStoreFactory;
import com.pinterest.soundwave.bean.EsDailySnapshotInstance;
import com.pinterest.soundwave.elasticsearch.EsDailySnapshotStoreFactory;

import org.apache.commons.collections.IteratorUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v2/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DailySnapshot {

  private static final Logger logger = LoggerFactory.getLogger(DailySnapshot.class);
  private DailySnapshotStoreFactory factory = new EsDailySnapshotStoreFactory();

  @GET
  @Path("/dailysnapshot/{day}")
  public Response getDailySnapshot(@PathParam("day")
                                   @NotNull String day) {

    OperationStats opStats = new OperationStats("cmdb_api", "get_dailysnapshot", new HashMap<>());
    Map<String, String> tags = new HashMap<>();

    try {
      DateTime time = DateTime.parse(day, DateTimeFormat.forPattern("yyyy-MM-dd"));
      DailySnapshotStore dailySnapshot = factory.getDailyStore(time);

      Iterator<EsDailySnapshotInstance>
          iter = dailySnapshot.getSnapshotInstances();
      List<EsDailySnapshotInstance> ret = IteratorUtils.toList(iter);

      logger.info("Success: getDailySnapshot - {}", day);
      return Response.status(Response.Status.OK)
          .type(MediaType.APPLICATION_JSON)
          .entity(ret)
          .build();

    } catch (Exception e) {

      return Utils.responseException(e, logger, opStats, tags);
    }

  }
}
