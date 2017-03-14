package com.pinterest.cmp.cmdb.elasticsearch;

import com.pinterest.cmp.cmdb.bean.EsDailySnapshotInstance;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pinterest.cmp.cmdb.pinterest.EsInstanceStore;

import org.junit.Assert;
import org.junit.Test;

public class EsPropertyNamingStrategyTest {

  private final String doc = " {" +
      "               \"region\": \"us-east-1\"," +
      "               \"nodepool\": \"coreapp-webapp-prod\"," +
      "               \"location\": \"us-east-1d\"," +
      "               \"svc_tag\": [" +
      "                  \"Web\"" +
      "               ]," +
      "               \"state\": \"terminated\"," +
      "               \"config\": {" +
      "                  \"pinternal_dns\": \"coreapp-webapp-prod-0a018ef5.use1.pin220.com\"," +
      "                  \"instance_lifecycle\": \"fixed\"," +
      "                  \"instance_type\": \"c3.8xlarge\"," +
      "                  \"internal_dns\": \"coreapp-webapp-prod-0a018ef5.ec2.shame\"," +
      "                  \"pexternal_dns\": \"coreapp-webapp-prod-0a018ef5.use1.pinadmin.com\"," +
      "                  \"name\": \"coreapp-webapp-prod-0a018ef5\"," +
      "                  \"kernel_id\": \"123\"," +
      "                  \"internal_address\": \"10.1.142.245\"," +
      "                  \"architecture\": \"x86_64\"," +
      "                  \"pinternal_domain\": \"use1.pin220.com\"," +
      "                  \"pexternal_domain\": \"use1.pinadmin.com\"," +
      "                  \"image_id\": \"ami-b36ba6de\"" +
      "               }," +
      "               \"usage_tag\": [" +
      "                  \"FrontEnd\"" +
      "               ]," +
      "               \"deployment\": \"coreapp\"," +
      "               \"sys_tag\": [" +
      "                  \"SRE\"" +
      "               ]," +
      "               \"service_mapping\": [" +
      "                  \"ngapp2\"" +
      "               ]," +
      "               \"aws_launch_time\": \"2016-06-26T23:48:24.000Z\"" +
      "}";

  @Test
  public void deserializeWithStrategy() throws Exception {
    ObjectMapper
        mapper =
        new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(new EsPropertyNamingStrategy(
                EsDailySnapshotInstance.class, EsInstanceStore.class))
            .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true);

    EsDailySnapshotInstance inst = mapper.readValue(doc, EsDailySnapshotInstance.class);
    Assert.assertEquals("coreapp-webapp-prod-0a018ef5", inst.getName());
    Assert.assertEquals("fixed", inst.getLifecycle());
    Assert.assertTrue(inst.getLaunchTime() != null);

  }

}


