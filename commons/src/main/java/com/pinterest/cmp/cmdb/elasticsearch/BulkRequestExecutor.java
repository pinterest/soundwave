package com.pinterest.cmp.cmdb.elasticsearch;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

import java.util.concurrent.TimeUnit;

/**
 * A simple wrapper for ElasticsSearch Bulk request
 */
public class BulkRequestExecutor implements BulkProcessor.Listener {

  private BulkProcessor processor;
  private BulkResponse response;
  private Throwable throwable;

  public BulkRequestExecutor(Client client) {
    processor = BulkProcessor.builder(client, this).build();
  }

  public BulkRequestExecutor(Client client, int bulkActions, long maxSizeMegaBytes,
                             int concurrentRequest,
                             TimeValue flushInterval) {
    processor =
        BulkProcessor.builder(client, this).setBulkActions(bulkActions)
            .setBulkSize(new ByteSizeValue(maxSizeMegaBytes, ByteSizeUnit.MB))
            .setConcurrentRequests(concurrentRequest).setFlushInterval(flushInterval).build();
  }

  @Override
  public void beforeBulk(long l, org.elasticsearch.action.bulk.BulkRequest bulkRequest) {
    this.response = null;
    this.throwable = null;
  }

  @Override
  public void afterBulk(long l, org.elasticsearch.action.bulk.BulkRequest bulkRequest,
                        BulkResponse bulkResponse) {
    this.response = bulkResponse;
  }

  @Override
  public void afterBulk(long l, org.elasticsearch.action.bulk.BulkRequest bulkRequest,
                        Throwable throwable) {
    this.throwable = throwable;
  }

  public BulkResponse execute() throws Exception {
    processor.awaitClose(300, TimeUnit.SECONDS);
    if (response == null && throwable != null) {
      throw new Exception(throwable);
    }

    return response;
  }

  public void add(ActionRequest request) {
    this.processor.add(request);
  }
}
