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
package com.pinterest.soundwave.pinterest;

import com.pinterest.soundwave.bean.EsAwsStatus;
import com.pinterest.soundwave.bean.EsDailySnapshotInstance;
import com.pinterest.soundwave.bean.EsInstance;
import com.pinterest.soundwave.bean.EsStatus;
import com.pinterest.soundwave.elasticsearch.CmdbInstanceStore;
import com.pinterest.soundwave.elasticsearch.EsIterator;
import com.pinterest.soundwave.elasticsearch.EsMapper;
import com.pinterest.soundwave.elasticsearch.ScrollableResponse;
import com.pinterest.soundwave.bean.EsQueryResult;
import com.pinterest.soundwave.elasticsearch.EsBulkResponseSummary;
import com.pinterest.soundwave.elasticsearch.EsPropertyNamingStrategy;
import com.pinterest.soundwave.elasticsearch.EsStore;
import com.pinterest.config.Configuration;

import com.amazonaws.regions.Region;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ElasticSearch
 */
public class EsInstanceStore extends EsStore implements CmdbInstanceStore {

  protected static final String
      INDEX =
      Configuration.getProperties().getString("es_instance_index");
  protected static final String DOCTYPE = "instance";
  protected static final int BATCHSIZE = 5000;
  protected static final ObjectMapper updateMapper =
      new ObjectMapper();
  private static final Logger logger = LoggerFactory.getLogger(EsInstanceStore.class);
  protected final ObjectMapper insertMapper =
      new ObjectMapper();
  protected final ObjectMapper essnapshotinstanceMapper =
      new ObjectMapper();
  public String indexName = INDEX;
  public String docType = DOCTYPE;
  private Class instanceClass = EsInstance.class;


  public EsInstanceStore() {
    this(Configuration.getProperties().getString("es_cluster_lb"),
        Configuration.getProperties().getInt("es_cluster_port"));

  }

  public EsInstanceStore(String host, int port) {
    super(host, port);
    //This is required to let ES create the mapping of Date instead of long
    insertMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    updateMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .addMixIn(getInstanceClass(), IgnoreCreatedTimeMixin.class);

    //Specific mapper to read EsDailySnapshotInstance from the index
    essnapshotinstanceMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setPropertyNamingStrategy(new EsPropertyNamingStrategy(
            EsDailySnapshotInstance.class, EsInstanceStore.class))
        .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true);

  }

  private static final DateTime getStartSinceDay(int days) {
    DateTime utcNow = new DateTime(DateTimeZone.UTC);
    DateTime
        today =
        new DateTime(utcNow.getYear(), utcNow.getMonthOfYear(), utcNow.getDayOfMonth(), 0, 0, 0,
            DateTimeZone.UTC);
    return today.minusDays(days);
  }

  public Class getInstanceClass() {
    return instanceClass;
  }

  protected void setInstanceClass(Class instanceClass) {
    this.instanceClass = instanceClass;
  }

  public void setDocType(String docType) {
    this.docType = docType;
  }

  @Override
  public String getIndexName() {
    return indexName;
  }

  public void setIndexName(String indexName) {
    this.indexName = indexName;
  }

  @Override
  public String getDocTypeName() {
    return docType;
  }

  @Override
  public EsInstance getInstanceById(String instanceId) throws Exception {
    return this.getInstanceByIdForClass(instanceId, getInstanceClass());
  }

  @Override
  public long updateOrInsertInstance(EsInstance instance) throws Exception {
    UpdateResponse
        response =
        this.updateOrInsert(instance.getId(),
            updateMapper.writeValueAsBytes(instance),
            insertMapper.writeValueAsBytes(instance));
    return response.isCreated() ? 0 : response.getVersion();
  }

  @Override
  public long update(EsInstance instance) throws Exception {
    UpdateResponse
        response =
        this.update(instance.getId(), updateMapper.writeValueAsBytes(instance),
            instance.getVersion());
    return response.getVersion();
  }

  @Override
  public String checkStatus() throws Exception {

    final ClusterHealthResponse healthResponse = esClient
        .admin().cluster().prepareHealth().execute().actionGet();

    if (healthResponse.isTimedOut()) {
      return EsStatus.TIMEOUT.toString();

    } else if (ClusterHealthStatus.RED.equals(healthResponse.getStatus())) {
      logger.warn("Elastic search health status is reported RED");
      return EsStatus.ERROR.toString();

    } else if (ClusterHealthStatus.GREEN.equals(healthResponse.getStatus())) {
      return EsStatus.SUCCESS.toString();

    } else {
      logger.warn("Elastic search health status is unknown");
      return EsStatus.UNKNOWN.toString();
    }
  }

  @Override
  public Iterator<EsInstanceTags> getRunningAndTerminatedInstanceTags(int terminatedDays)
      throws Exception {

    Preconditions.checkArgument(terminatedDays >= 0);
    DateTime start = getStartSinceDay(terminatedDays);

    //state eq running or terminated after the start time.
    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .should(QueryBuilders.termQuery("state", "running"))
        .should(QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery("state", "terminated"))
            .must(QueryBuilders.rangeQuery("aws_launch_time").gte(start)))
        .minimumNumberShouldMatch(1);

    ScrollableResponse<List<EsInstanceTags>>
        response =
        this.retrieveScrollByQuery(queryBuilder,
            EsMapper.getIncludeFields(EsInstanceTags.class, this.getClass()), BATCHSIZE,
            str -> updateMapper.readValue(str, EsInstanceTags.class));

    EsIterator<EsInstanceTags>
        iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> updateMapper.readValue(str, EsInstanceTags.class)));
    return iterator;
  }

  @Override
  public Iterator<EsDailySnapshotInstance> getRunningAndRecentTerminatedInstances(int days)
      throws Exception {
    Preconditions.checkArgument(days >= 0);
    DateTime start = getStartSinceDay(days);

    //state eq running or terminated after the start time.
    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .should(QueryBuilders.termQuery("state", "running"))
        .should(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("state", "terminated"))
            .must(QueryBuilders.rangeQuery("aws_launch_time").gte(start)))
        .minimumNumberShouldMatch(1);

    ScrollableResponse<List<EsDailySnapshotInstance>>
        response =
        this.retrieveScrollByQuery(queryBuilder,
            EsMapper.getIncludeFields(EsDailySnapshotInstance.class, this.getClass()),
            BATCHSIZE,
            str -> essnapshotinstanceMapper.readValue(str, EsDailySnapshotInstance.class));

    EsIterator<EsDailySnapshotInstance>
        iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> essnapshotinstanceMapper.readValue(str, EsDailySnapshotInstance.class)));
    return iterator;
  }

  @Override
  public void bulkInsert(List<EsInstance> instances) throws Exception {
    EsBulkResponseSummary responses = super.bulkInsert(instances, BATCHSIZE,
        insertMapper);

    logger.info("Bulk insert on {} entries. {} succeeded and {} failed", instances.size(),
        responses.getSucceeded().size(), responses.getFailed().size());
    if (responses.getFailed().size() > 0) {
      throw new RuntimeException(responses.getFailedMessage(5));
    }
  }

  @Override
  public void bulkUpdate(List<EsInstance> instances) throws Exception {
    EsBulkResponseSummary
        responses =
        super.bulkUpdate(instances, BATCHSIZE, updateMapper);
    logger.info("Bulk update on {} entries. {} succeeded and {} failed", instances.size(),
        responses.getSucceeded().size(), responses.getFailed().size());
    if (responses.getFailed().size() > 0) {
      String error = responses.getFailedMessage(5);
      if (StringUtils.isNotEmpty(error)) {
        throw new RuntimeException(responses.getFailedMessage(5));
      }
    }
  }

  @Override
  public void bulkUpdateInstanceTags(List<EsInstanceTags> instances) throws Exception {
    EsBulkResponseSummary
        responses =
        super.bulkUpdate(instances, BATCHSIZE, updateMapper);
    logger.info("Bulk update on {} entries. {} succeeded and {} failed", instances.size(),
        responses.getSucceeded().size(), responses.getFailed().size());
    if (responses.getFailed().size() > 0) {
      throw new RuntimeException(responses.getFailedMessage(5));
    }
  }

  @Override
  public void bulkUpdateAwsStatus(List<EsAwsStatus> awsStatuses) throws Exception {
    EsBulkResponseSummary
        responses = super.bulkUpdate(awsStatuses, BATCHSIZE, updateMapper);

    logger.info("Bulk update on {} entries. {} succeded and {} failed", awsStatuses.size(),
        responses.getSucceeded().size(), responses.getFailed().size());

    if (responses.getFailed().size() > 0) {
      throw new RuntimeException(responses.getFailedMessage(5));
    }
  }

  @Override
  public Iterator<EsInstance> getRunningInstances() throws Exception {

    //state eq running and in the specificied region
    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .must(QueryBuilders.termQuery("state", "running"));

    ScrollableResponse<List<EsInstance>> response = this.retrieveScrollByQuery(queryBuilder,
        EsMapper.getIncludeFields(getInstanceClass()), BATCHSIZE,
        str -> (EsInstance) insertMapper.readValue(str, getInstanceClass()));

    EsIterator<EsInstance>
        iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> (EsInstance) insertMapper.readValue(str, getInstanceClass())));
    return iterator;
  }

  @Override
  public Iterator<EsInstance> getRunningInstances(Region region) throws Exception {
    return getRunningInstances(region, EsMapper.getIncludeFields(getInstanceClass()));
  }

  @Override
  public Iterator<EsInstance> getRunningInstances(Region region, String[] fields) throws Exception {

    Preconditions.checkNotNull(region);

    //state eq running and in the specificied region
    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .must(QueryBuilders.termQuery("state", "running"))
        .must(QueryBuilders.termQuery("region", region.getName().toLowerCase()));

    ScrollableResponse<List<EsInstance>> response = this.retrieveScrollByQuery(queryBuilder,
        fields, BATCHSIZE,
        str -> (EsInstance) insertMapper.readValue(str, getInstanceClass()));

    EsIterator<EsInstance>
        iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> (EsInstance) insertMapper.readValue(str, getInstanceClass())));
    return iterator;
  }

  @Override
  public Iterator<EsInstance> getRecentlyTerminatedInstances(Region region,
                                                             int days) throws Exception {

    Preconditions.checkNotNull(region);
    Preconditions.checkArgument(days > 0);

    DateTime start = getStartSinceDay(days);

    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .must(QueryBuilders.termQuery("region", region.getName().toLowerCase()))
        .must(QueryBuilders.termQuery("state", "terminated"))
        .must(QueryBuilders.rangeQuery("aws_launch_time").gte(start));

    ScrollableResponse<List<EsInstance>> response = this.retrieveScrollByQuery(queryBuilder,
        EsMapper.getIncludeFields(getInstanceClass()), BATCHSIZE,
        str -> (EsInstance) insertMapper.readValue(str, getInstanceClass()));

    EsIterator<EsInstance>
        iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> (EsInstance) insertMapper.readValue(str, getInstanceClass())));

    return iterator;
  }

  @Override
  public Iterator<EsAwsStatus> getRunningAndTerminatedAwsStatus(Region region, int days)
      throws Exception {
    Preconditions.checkNotNull(region);
    Preconditions.checkArgument(days > 0);

    DateTime start = getStartSinceDay(days);

    //state eq running or terminated after the start time.
    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .should(QueryBuilders.termQuery("state", "running"))
        .must(QueryBuilders.termQuery("region", region.getName().toLowerCase()))
        .should(QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery("state", "terminated"))
            .must(QueryBuilders.termQuery("region", region.getName().toLowerCase()))
            .must(QueryBuilders.rangeQuery("aws_launch_time").gte(start)))
        .minimumNumberShouldMatch(1);

    ScrollableResponse<List<EsAwsStatus>> response =
        this.retrieveScrollByQuery(queryBuilder,
            EsMapper.getIncludeFields(EsAwsStatus.class, this.getClass()), BATCHSIZE,
            str -> updateMapper.readValue(str, EsAwsStatus.class));

    EsIterator<EsAwsStatus> iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> updateMapper.readValue(str, EsAwsStatus.class)));

    return iterator;
  }

  @Override
  public EsAwsStatus getAwsStatus(String instanceId) throws Exception {

    SearchResponse response = this.retrieveByField("id", instanceId, EsAwsStatus.class);
    if (response.getHits().totalHits() > 0) {

      String str = response.getHits().getAt(0).getSourceAsString();
      EsAwsStatus status = insertMapper.readValue(str, EsAwsStatus.class);
      return status;
    }
    return null;

  }

  @Override
  public Iterator<EsMetaData> getMetaData(String fieldName, String fieldValue)
      throws Exception {

    Preconditions.checkNotNull(fieldName);
    Preconditions.checkNotNull(fieldValue);

    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .must(QueryBuilders.termQuery(fieldName, fieldValue))
        .must(QueryBuilders.termQuery("state", "running"));

    ScrollableResponse<List<EsMetaData>> response =
        this.retrieveScrollByQuery(queryBuilder,
            EsMapper.getIncludeFields(EsMetaData.class, this.getClass()), BATCHSIZE,
            str -> updateMapper.readValue(str, EsMetaData.class));

    EsIterator<EsMetaData> iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> updateMapper.readValue(str, EsMetaData.class)));

    return iterator;
  }

  @Override
  public Iterator<EsInstance> getInstanceCreatedBetween(Date start, Date end) throws Exception {
    Preconditions.checkArgument(start.getTime() < end.getTime());
    QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(
        QueryBuilders.rangeQuery("created_time").from(start).to(end).includeLower(true)
            .includeUpper(false)
    );
    ScrollableResponse<List<EsInstance>> response = this.retrieveScrollByQuery(queryBuilder,
        EsMapper.getIncludeFields(getInstanceClass()), BATCHSIZE,
        str -> (EsInstance) insertMapper.readValue(str, getInstanceClass()));

    EsIterator<EsInstance>
        iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> (EsInstance) insertMapper.readValue(str, getInstanceClass())));

    return iterator;
  }


  public Map<String, NodeStats> getNodesStats() throws Exception {
    NodesStatsResponse nodesStats = esClient.admin().cluster().prepareNodesStats().all().get();
    return nodesStats.getNodesMap();
  }

  public Map<String, IndexStats> getIndexStats() throws Exception {
    IndicesStatsResponse resp = esClient.admin().indices().prepareStats().all().get();
    return resp.getIndices();
  }

  /**
   * @param query         is the string query provided by user
   * @param includeFields in the output
   * @return Iterator of EsQueryResults
   * @throws Exception
   */
  @Override
  public Iterator<EsQueryResult> query(String query, String[] includeFields) throws Exception {

    Preconditions.checkNotNull(query);

    QueryBuilder queryBuilder = QueryBuilders.queryString(query);
    ScrollableResponse<List<EsQueryResult>> response = this.retrieveScrollByQuery(queryBuilder,
        includeFields, BATCHSIZE,
        str -> insertMapper.readValue(str, EsQueryResult.class));

    EsIterator<EsQueryResult>
        iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> insertMapper.readValue(str, EsQueryResult.class)));

    return iterator;
  }

  @Override
  public Iterator<EsQueryResult> getRunningInstancesWithFields(String[] includeFields)
      throws Exception {

    QueryBuilder queryBuilder = QueryBuilders.filteredQuery(
        QueryBuilders.termQuery("state", "running"),
        FilterBuilders.termFilter("state", "running"));

    ScrollableResponse<List<EsQueryResult>> response = this.retrieveScrollByQuery(queryBuilder,
        includeFields, BATCHSIZE,
        str -> insertMapper.readValue(str, EsQueryResult.class));

    EsIterator<EsQueryResult>
        iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> insertMapper.readValue(str, EsQueryResult.class)));

    return iterator;
  }


  @Override
  public Iterator<EsNameMetaData> getMetaDataByName(String field, String name) throws Exception {

    Preconditions.checkNotNull(field);
    Preconditions.checkNotNull(name);

    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .must(QueryBuilders.termQuery(field, name))
        .must(QueryBuilders.termQuery("state", "running"));

    ScrollableResponse<List<EsNameMetaData>> response =
        this.retrieveScrollByQuery(queryBuilder,
            EsMapper.getIncludeFields(EsNameMetaData.class, this.getClass()), BATCHSIZE,
            str -> updateMapper.readValue(str, EsNameMetaData.class));

    EsIterator<EsNameMetaData> iterator =
        new EsIterator<>(response, r -> scrollNext(r.getContinousToken(),
            str -> updateMapper.readValue(str, EsNameMetaData.class)));

    return iterator;
  }


  @Override
  public Map<String, HashMap> getAggregations(List<String> aggregationParams) throws Exception {

    // Return null if list is empty
    if (aggregationParams.size() == 0) {
      return null;
    }

    List<AggregationBuilder> aggregationBuilders = new ArrayList<>();
    for (int i = 0; i < aggregationParams.size(); i++) {

      aggregationBuilders.add(AggregationBuilders
          .terms(aggregationParams.get(i))
          .field(aggregationParams.get(i))
          .size(0));
    }

    AggregationBuilder finalAggregation;

    // If there is more than one aggregation builder merge them
    if (aggregationBuilders.size() > 1) {
      for (int i = aggregationBuilders.size() - 2; i >= 0; i--) {

        // Get the builder at ith position
        AggregationBuilder aggregationBuilder = aggregationBuilders.get(i);

        // Add its successor as sub aggregation
        aggregationBuilder.subAggregation(aggregationBuilders.get(i + 1));

        // add back the updated aggregationbui  dlder
        aggregationBuilders.add(i, aggregationBuilder);
      }
    }

    // Get the aggregation at the front of List
    finalAggregation = aggregationBuilders.get(0);

    SearchResponse response = esClient.prepareSearch(INDEX).setSize(0)
        .addAggregation(finalAggregation)
        .execute()
        .actionGet();

    String currentParam = aggregationParams.get(0);
    Terms terms = response.getAggregations().get(currentParam);
    Collection<Terms.Bucket> buckets = terms.getBuckets();

    Map<String, HashMap> results = new HashMap<>();
    results.put(currentParam, processBucketData(aggregationParams, currentParam, buckets));

    return results;
  }

  /**
   * This a recursive function to process data and produce a hashmap of subaggregations
   * @param aggregationParams List of all aggregation parameters
   * @param currentParam Current parameter to use for processing buckets
   * @param buckets Collection of Terms.Bucket
   * @return HashMap of results
   * @throws Exception
   */
  private HashMap<String, HashMap> processBucketData(
      List<String> aggregationParams, String currentParam, Collection<Terms.Bucket> buckets)
      throws Exception {

    HashMap<String, HashMap> result = new HashMap<>();

    for (Terms.Bucket bucket : buckets) {

      String bucketKey = bucket.getKey();
      long docCount = bucket.getDocCount();

      result.put(bucketKey, new HashMap());
      result.get(bucketKey).put("count", docCount);

      // Check if we are at the end of aggregation list
      int index = aggregationParams.indexOf(currentParam);

      if (index == aggregationParams.size() - 1) {
        // We have reached to the end of list stop processing and move to next bucket
        continue;

      }

      String nextParam = aggregationParams.get(index + 1);
      Terms terms = bucket.getAggregations().get(nextParam);
      Collection<Terms.Bucket> subBuckets = terms.getBuckets();

      // use of recursion to get subbuckets hashmap
      result.get(bucketKey)
          .put(nextParam, processBucketData(aggregationParams, nextParam, subBuckets));
    }

    return result;
  }


  protected <E extends EsInstance> E getInstanceByIdForClass(String instanceId, Class objectClass)
      throws Exception {

    SearchResponse response = this.retrieveByField("id", instanceId, objectClass);
    if (response.getHits().totalHits() > 0) {

      String str = response.getHits().getAt(0).getSourceAsString();
      E inst = (E) insertMapper.readValue(str, objectClass);
      inst.setVersion(response.getHits().getAt(0).getVersion());
      return inst;
    }
    return null;
  }


  public abstract class IgnoreCreatedTimeMixin {

    @JsonProperty("created_time")
    @JsonIgnore
    private Date createdTime;

    @JsonIgnore
    abstract Date getCreatedTime();

  }

  // End of class
}
