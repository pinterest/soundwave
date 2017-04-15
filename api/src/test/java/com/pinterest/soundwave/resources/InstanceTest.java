package com.pinterest.soundwave.resources;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.pinterest.soundwave.api.EsInstanceAdapter;
import com.pinterest.soundwave.utils.ObjectAdapter;
import com.pinterest.soundwave.bean.EsInstance;
import com.pinterest.soundwave.elasticsearch.CmdbInstanceStore;
import com.pinterest.soundwave.pinterest.EsInstanceStore;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RunWith(MockitoJUnitRunner.class)
public class InstanceTest {

  private static final CmdbInstanceStore cmdbInstanceStore = mock(CmdbInstanceStore.class);
  private static final Logger logger = LoggerFactory.getLogger(Instance.class);
  @ClassRule
  public static final ResourceTestRule RESOURCES = ResourceTestRule.builder()
      .addResource(new Instance(cmdbInstanceStore))
      .build();

  @Before
  public void setUp() {
  }

  @Test
  public void unknownInstanceId() {

    String instanceId ="i-abc";
    String url = "/v2/instances/status/"+instanceId;
    Response response = RESOURCES.client().target(url)
        .request(MediaType.APPLICATION_JSON)
        .get();

    Map status = response.readEntity(Map.class);
    int intStatus = ((Integer) status.get(instanceId)).intValue();

    assertThat(intStatus == -1);
    assertThat(response.getStatus() == 200);

  }

  @Test
  @Ignore
  public void getInstance() throws Exception{
    System.setProperty("config.file", "config/soundwave.properties");
    String id = "i-0bf9590a6b147f07b";
    CmdbInstanceStore store = new EsInstanceStore();
    EsInstance inst = store.getInstanceById(id);
    EsInstanceAdapter esInstanceAdapter = ObjectAdapter.getObject(inst, EsInstanceAdapter.class);
    Assert.assertTrue(esInstanceAdapter!=null);


  }
  @Test
  @Ignore
  public void knownInstanceId(){

    try {

      URL getInstanceUrl = new URL("http://soundwave.pinadmin.com/api/soundwave/getinstance");
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> jsonResponse = mapper.readValue(getInstanceUrl, Map.class);

      List<String> instanceList = (List) jsonResponse.get("possible_instances");

      // Select the random 10th instance id that soundwave returned.
      String instanceId = instanceList.get(10);

      String url = "/v2/instances/status/"+instanceId;
      Response response = RESOURCES.client().target(url)
          .request(MediaType.APPLICATION_JSON)
          .get();

      Map status = response.readEntity(Map.class);
      int intStatus = ((Integer) status.get(instanceId)).intValue();

      assertThat(intStatus == 0 || intStatus == 1);
      assertThat(response.getStatus() == 200);

    } catch (JsonParseException e) {
      e.printStackTrace();
      logger.warn(ExceptionUtils.getRootCauseMessage(e));
    } catch (JsonMappingException e) {
      e.printStackTrace();
      logger.warn(ExceptionUtils.getRootCauseMessage(e));
    } catch (MalformedURLException e) {
      e.printStackTrace();
      logger.warn(ExceptionUtils.getRootCauseMessage(e));
    } catch (IOException e) {
      e.printStackTrace();
      logger.warn(ExceptionUtils.getRootCauseMessage(e));
    }
  }
}
