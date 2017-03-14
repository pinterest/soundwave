package com.pinterest.cmp.cmdb.elasticsearch;

import com.pinterest.cmp.cmdb.aws.DailySnapshotStore;
import com.pinterest.cmp.cmdb.bean.EsDailySnapshotInstance;
import com.pinterest.cmp.config.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * The Elasticsearch index corresponding to each day
 */
public class EsDailySnapshotStore extends EsStore implements DailySnapshotStore {

  private static final Logger logger = LoggerFactory.getLogger(EsDailySnapshotStore.class);

  private static final int BATCHSIZE = 5000;
  private static final String
      INDEX =
      Configuration.getProperties().getString("es_daily_snapshot_index");
  private final ObjectMapper
      essnapshotinstanceMapper =
      new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .setPropertyNamingStrategy(new EsPropertyNamingStrategy(
              EsDailySnapshotInstance.class, EsDailySnapshotStore.class))
          .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true);

  private String day;

  public EsDailySnapshotStore(DateTime day) {
    this.day = day.toString("yyyy-MM-dd");
  }

  @Override
  public String getIndexName() {
    return INDEX;
  }

  @Override
  public String getDocTypeName() {
    return day;
  }


  @Override
  public EsDailySnapshotInstance getInstanceById(String instanceId) throws Exception {
    SearchResponse
        response =
        this.retrieveByField("_id", instanceId, EsDailySnapshotInstance.class);
    if (response.getHits().totalHits() > 0) {

      String str = response.getHits().getAt(0).getSourceAsString();
      EsDailySnapshotInstance
          inst =
          essnapshotinstanceMapper.readValue(str, EsDailySnapshotInstance.class);
      inst.setId(response.getHits().getAt(0).getId());
      inst.setVersion(response.getHits().getAt(0).getVersion());
      return inst;
    }
    return null;
  }


  @Override
  public long updateOrInsert(EsDailySnapshotInstance instance) throws Exception {
    byte[] doc = essnapshotinstanceMapper.writeValueAsBytes(instance);
    UpdateResponse
        response =
        this.updateOrInsert(instance.getId(), doc,
            doc);
    return response.getVersion();
  }

  @Override
  public long update(EsDailySnapshotInstance instance) throws Exception {
    byte[] doc = essnapshotinstanceMapper.writeValueAsBytes(instance);
    UpdateResponse
        response =
        this.update(instance.getId(), doc, instance.getVersion());
    return response.getVersion();
  }

  @Override
  public Iterator<EsDailySnapshotInstance> getSnapshotInstances() throws Exception {
    ScrollableResponse<List<EsDailySnapshotInstance>>
        response =
        this.retrieveAll(EsMapper.getIncludeFields(EsDailySnapshotInstance.class, this.getClass()),
            BATCHSIZE,
            str -> essnapshotinstanceMapper.readValue(str, EsDailySnapshotInstance.class));
    EsIterator<EsDailySnapshotInstance>
        iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> essnapshotinstanceMapper.readValue(str, EsDailySnapshotInstance.class)));
    return iterator;
  }

  @Override
  public void bulkInsert(List<EsDailySnapshotInstance> instances) throws Exception {
    EsBulkResponseSummary responses = super.bulkInsert(instances, BATCHSIZE,
        essnapshotinstanceMapper);

    logger.info("Bulk insert on {} entries. {} succeeded and {} failed", instances.size(),
        responses.getSucceeded().size(), responses.getFailed().size());
    if (responses.getFailed().size() > 0) {
      throw new RuntimeException(responses.getFailedMessage(5));
    }


  }

  @Override
  public void bulkUpdate(List<EsDailySnapshotInstance> instances) throws Exception {
    EsBulkResponseSummary
        responses =
        super.bulkUpdate(instances, BATCHSIZE, essnapshotinstanceMapper);
    logger.info("Bulk update on {} entries. {} succeeded and {} failed", instances.size(),
        responses.getSucceeded().size(), responses.getFailed().size());
    if (responses.getFailed().size() > 0) {
      throw new RuntimeException(responses.getFailedMessage(5));
    }

  }

}
