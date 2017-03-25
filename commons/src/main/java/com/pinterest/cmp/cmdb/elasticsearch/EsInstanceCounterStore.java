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
package com.pinterest.cmp.cmdb.elasticsearch;


import com.pinterest.cmp.cmdb.bean.EsInstanceCountRecord;
import com.pinterest.cmp.config.Configuration;

import com.google.common.base.Preconditions;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class EsInstanceCounterStore extends EsStore implements InstanceCounterStore{

  private static final Logger logger = LoggerFactory.getLogger(EsInstanceCounterStore.class);
  private static final String INDEX = Configuration.getProperties().getString("es_instance_index");
  private static final String DOCTYPE = "instancecounter";
  private final ObjectMapper insertMapper = new ObjectMapper();
  private static final int BATCHSIZE = 5000;

  public String indexName = INDEX;
  public String docType = DOCTYPE;

  // Override for EsStore methods.
  @Override
  public String getIndexName() {
    return indexName;
  }

  @Override
  public String getDocTypeName() {
    return docType;
  }

  public EsInstanceCounterStore() {
    this(Configuration.getProperties().getString("es_cluster_lb"),
        Configuration.getProperties().getInt("es_cluster_port"));

  }

  public EsInstanceCounterStore(String host, int port) {

    super(host, port);

    //This is required to let ES create the mapping of Date instead of long
    insertMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  }

  /**
   * This function writes a list of instancerecords to cmdb store
   * @param records list of reserved instance count records
   * @throws Exception
   */
  @Override
  public void bulkInsert(List<EsInstanceCountRecord> records) throws Exception {
    EsBulkResponseSummary responses = super.bulkInsert(records, BATCHSIZE,
        insertMapper);

    logger.info("Bulk insert on {} entries. {} succeeded and {} failed", records.size(),
        responses.getSucceeded().size(), responses.getFailed().size());
    if (responses.getFailed().size() > 0) {
      throw new RuntimeException(responses.getFailedMessage(5));
    }
  }


  /**
   * Get a list of instance count records for a given date
   * @param date Date for which instance count records are requested
   * @return List of EsInstanceCountRecord
   * @throws Exception
   */
  @Override
  public Iterator<EsInstanceCountRecord> getCountRecordsByDate(Date date) throws Exception {

    Preconditions.checkNotNull(date);
    String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date);

    // Supply same date for both gte and lte to get records for that day.
    QueryBuilder queryBuilder = QueryBuilders.rangeQuery("date_time")
        .gte(dateStr).lte(dateStr);

    ScrollableResponse<List<EsInstanceCountRecord>> response =
        this.retrieveScrollByQuery(queryBuilder,
            EsMapper.getIncludeFields(EsInstanceCountRecord.class, this.getClass()), BATCHSIZE,
            str -> insertMapper.readValue(str, EsInstanceCountRecord.class));

    EsIterator<EsInstanceCountRecord> iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> insertMapper.readValue(str, EsInstanceCountRecord.class)));

    return iterator;
  }


}
