package com.pinterest.cmp.cmdb.aws;

import com.pinterest.cmp.cmdb.bean.EsInstance;

import com.amazonaws.services.ec2.model.Instance;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEsInstanceFactory {

  protected ObjectMapper mapper = new ObjectMapper();

  public abstract EsInstance createFromEC2(Instance awsInstance) throws Exception;

  public void setCloudInstanceStore(CloudInstanceStore store) {}

  protected HashMap getAwsInstanceProperties(Instance awsInstance) throws Exception {
    HashMap map = mapper.readValue(mapper.writeValueAsString(awsInstance), HashMap.class);

    if (awsInstance.getMonitoring() != null && awsInstance.getMonitoring().getState() != null) {
      //Have to comply with the current AWS_V1 schema
      map.put("monitoring", awsInstance.getMonitoring().getState().toString());
    }

    if (awsInstance.getPlacement() != null
        && awsInstance.getPlacement().getAvailabilityZone() != null) {
      //Be backward compatible for tools
      Map placement = (Map) map.get("placement");
      if (placement != null) {
        placement.put("availability_zone", awsInstance.getPlacement().getAvailabilityZone());
      }
    }
    return map;
  }
}
