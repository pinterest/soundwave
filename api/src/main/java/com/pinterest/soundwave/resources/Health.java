package com.pinterest.soundwave.resources;

import com.pinterest.OperationStats;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


@Path("/_/_/")
public class Health {

  @GET
  public Response healthCheckGet() {

    OperationStats opStats = new OperationStats("cmdb_api", "elb_health_check");
    opStats.succeed();

    return Response.status(Response.Status.OK).build();
  }
}
