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
package com.pinterest.soundwave.elasticsearch;

import com.pinterest.soundwave.bean.EsDocument;
import com.pinterest.soundwave.utils.ThrowingFunction;
import com.pinterest.config.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;

public abstract class EsStore {

  private static final int SCROLLDEFAULTTIMEOUT = 60000;
  protected Client esClient;

  public EsStore() {
    this(Configuration.getProperties().getString("es_cluster_lb"),
        Configuration.getProperties().getInt("es_cluster_port"));
  }

  public EsStore(String host, int port) {
    String clusterName = Configuration.getProperties().getString("es_cluster_name", "soundwave");
    Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", clusterName)
        .put("client.transport.sniff", false).build();
    esClient = new TransportClient(settings)
        .addTransportAddress(
            new InetSocketTransportAddress(host, port));
  }

  public abstract String getIndexName();

  public abstract String getDocTypeName();

  protected SearchResponse retrieveByField(String field, Object value, Class type)
      throws Exception {
    return this.retrieveByField(field, value, EsMapper.getIncludeFields(type));
  }

  protected SearchResponse retrieveByField(String field, Object value, String[] includeFields)
      throws Exception {
    // The query here is using the exact value match.
    // https://www.elastic.co/guide/en/elasticsearch/guide/1.x/_finding_exact_values.html
    SearchRequestBuilder builder = esClient.prepareSearch()
        .setIndices(getIndexName()).setTypes(getDocTypeName())
        .setQuery(QueryBuilders
            .filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.termsFilter(field, value)))
        .setFetchSource(includeFields, null).setVersion(true);

    return builder.execute().actionGet();
  }


  protected <E extends EsDocument> ScrollableResponse<List<E>> retrieveAll(
      String[] includeFields, int size, ThrowingFunction<String, E> createFunc)
      throws Exception {

    Preconditions.checkArgument(size > 0);

    SearchRequestBuilder builder = esClient.prepareSearch()
        .setIndices(getIndexName()).setTypes(getDocTypeName())
        .setScroll(new TimeValue(SCROLLDEFAULTTIMEOUT))
        .setSize(size)
        .setFetchSource(includeFields, null).setVersion(true);

    SearchResponse response = builder.execute().actionGet();
    return convertToScrollableResponse(response, createFunc);
  }

  protected <E extends EsDocument> ScrollableResponse<List<E>> retrieveScrollByField(
      String field, Object value, String[] includeFields, int size,
      ThrowingFunction<String, E> createFunc)
      throws Exception {

    Preconditions.checkArgument(size > 0);

    SearchRequestBuilder builder = esClient.prepareSearch()
        .setIndices(getIndexName()).setTypes(getDocTypeName())
        .setScroll(new TimeValue(SCROLLDEFAULTTIMEOUT))
        .setSize(size)
        .setQuery(QueryBuilders
            .filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.termsFilter(field, value)))
        .setFetchSource(includeFields, null).setVersion(true);

    SearchResponse response = builder.execute().actionGet();
    return convertToScrollableResponse(response, createFunc);
  }

  protected <E extends EsDocument> ScrollableResponse<List<E>> retrieveScrollByQuery(
      QueryBuilder queryBuilder, String[] includeFields, int size,
      ThrowingFunction<String, E> createFunc)
      throws Exception {

    Preconditions.checkArgument(size > 0);

    SearchRequestBuilder builder = esClient.prepareSearch()
        .setIndices(getIndexName()).setTypes(getDocTypeName())
        .setScroll(new TimeValue(SCROLLDEFAULTTIMEOUT))
        .setSize(size)
        .setQuery(queryBuilder)
        .setFetchSource(includeFields, null).setVersion(true);

    SearchResponse response = builder.execute().actionGet();
    return convertToScrollableResponse(response, createFunc);
  }

  protected <E extends EsDocument> ScrollableResponse<List<E>> scrollNext(
      String scrollId, ThrowingFunction<String, E> createFunc)
      throws Exception {
    SearchScrollRequestBuilder
        builder =
        esClient.prepareSearchScroll(scrollId)
            .setScroll(TimeValue.timeValueMillis(SCROLLDEFAULTTIMEOUT));
    SearchResponse response = builder.execute().actionGet();
    ArrayList<E> list = new ArrayList<>();
    ScrollableResponse<List<E>> ret = new ScrollableResponse<>();
    ret.setValue(list);
    ret.setContinousToken(response.getScrollId());
    if (response.getHits().totalHits() == 0) {
      //Clear the scroll as early as possible to save resource
      ClearScrollRequestBuilder
          clearRequestBuilder =
          esClient.prepareClearScroll().addScrollId(scrollId);
      clearRequestBuilder.execute();
      ret.setScrollToEnd(true);
    } else {
      for (int i = 0; i < response.getHits().getHits().length; i++) {
        String str = response.getHits().getAt(i).getSourceAsString();
        E element = createFunc.apply(str);
        element.setId(response.getHits().getAt(i).getId());
        list.add(element);
      }
    }
    return ret;
  }

  protected SearchResponse getByDocType() throws Exception {
    return getByDocType(1000);
  }

  protected SearchResponse getByDocType(int size) throws Exception {
    SearchRequestBuilder builder = esClient.prepareSearch();
    builder.setIndices(getIndexName()).setTypes(getDocTypeName())
        .setQuery(QueryBuilders.matchAllQuery())
        .setSize(size);

    return builder.execute().actionGet();
  }

  protected UpdateResponse updateOrInsert(String id, byte[] updateDoc, byte[] insertDoc)
      throws Exception {
    IndexRequest
        indexRequest =
        new IndexRequest(getIndexName(), getDocTypeName(), id).source(insertDoc);

    UpdateRequest
        updateRequest =
        new UpdateRequest(getIndexName(), getDocTypeName(), id).doc(updateDoc).upsert(indexRequest);
    return esClient.update(updateRequest).actionGet();
  }

  protected UpdateResponse update(String id, byte[] doc, long version) {
    UpdateRequest
        updateRequest =
        new UpdateRequest(getIndexName(), getDocTypeName(), id).doc(doc).version(version);
    return esClient.update(updateRequest).actionGet();
  }

  protected UpdateResponse update(String id, byte[] doc) {
    UpdateRequest
        updateRequest =
        new UpdateRequest(getIndexName(), getDocTypeName(), id).doc(doc);
    return esClient.update(updateRequest).actionGet();
  }

  protected <E extends EsDocument> EsBulkResponseSummary bulkInsert(List<E> insertDoc,
                                                                    int batchSize,
                                                                    ObjectMapper mapper)
      throws Exception {
    Preconditions.checkArgument(batchSize > 0);
    List<BulkResponse> responses = new ArrayList<>();
    int count = 0;
    BulkRequestExecutor executor = new BulkRequestExecutor(this.esClient);
    for (E doc : insertDoc) {
      executor
          .add(new IndexRequest(getIndexName(), getDocTypeName())
              .source(mapper.writeValueAsBytes(doc)).id(doc.getId()));
      count++;
      if (count >= batchSize) {
        responses.add(executor.execute());
        count = 0;
        executor = new BulkRequestExecutor(this.esClient);
      }
    }

    if (count > 0) {
      responses.add(executor.execute());
    }

    return new EsBulkResponseSummary(responses);
  }

  protected <E extends EsDocument> EsBulkResponseSummary bulkUpdate(List<E> updateDoc,
                                                                    int batchSize,
                                                                    ObjectMapper mapper)
      throws Exception {
    Preconditions.checkArgument(batchSize > 0);
    List<BulkResponse> responses = new ArrayList<>();
    int count = 0;
    BulkRequestExecutor executor = new BulkRequestExecutor(this.esClient);
    for (E doc : updateDoc) {
      if (doc.getVersion() > 0) {
        executor
            .add(new UpdateRequest(getIndexName(), getDocTypeName(), doc.getId())
                .doc(mapper.writeValueAsBytes(doc)).version(doc.getVersion()));
      } else {
        executor
            .add(new UpdateRequest(getIndexName(), getDocTypeName(), doc.getId())
                .doc(mapper.writeValueAsBytes(doc)));
      }
      count++;
      if (count >= batchSize) {
        responses.add(executor.execute());
        count = 0;
        executor = new BulkRequestExecutor(this.esClient);
      }
    }

    if (count > 0) {
      responses.add(executor.execute());
    }

    return new EsBulkResponseSummary(responses);
  }

  protected void close() {
    if (this.esClient != null) {
      this.esClient.close();
    }
  }

  private <E extends EsDocument> ScrollableResponse<List<E>> convertToScrollableResponse(
      SearchResponse response,
      ThrowingFunction<String, E>
          createFunc)
      throws Exception {
    ArrayList<E> list = new ArrayList<>();
    ScrollableResponse<List<E>> ret = new ScrollableResponse<>();
    ret.setValue(list);
    ret.setContinousToken(response.getScrollId());

    for (int i = 0; i < response.getHits().getHits().length; i++) {
      String str = response.getHits().getAt(i).getSourceAsString();
      E element = createFunc.apply(str);
      element.setId(response.getHits().getAt(i).getId());
      element.setVersion(response.getHits().getAt(i).getVersion());
      list.add(element);
    }
    return ret;
  }
}
