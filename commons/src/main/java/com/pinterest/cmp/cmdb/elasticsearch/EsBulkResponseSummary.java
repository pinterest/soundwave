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

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EsBulkResponseSummary {

  private Map<String, String> succeeded = new HashMap<>();
  private Map<String, String> failed = new HashMap<>();

  public EsBulkResponseSummary(List<BulkResponse> responses) {
    for (BulkResponse br : responses) {
      for (BulkItemResponse itemResponse : br.getItems()) {
        if (itemResponse.isFailed()) {
          failed.put(getItemId(itemResponse), itemResponse.getFailureMessage());
        } else {
          succeeded.put(getItemId(itemResponse), itemResponse.getOpType());
        }
      }
    }
  }

  public Map<String, String> getSucceeded() {
    return succeeded;
  }

  public Map<String, String> getFailed() {
    return failed;
  }

  private String getItemId(BulkItemResponse response) {
    String id = response.getId();
    if (response.getResponse() instanceof UpdateResponse) {
      id = ((UpdateResponse) response.getResponse()).getId();
    } else if (response.getResponse() instanceof IndexResponse) {
      id = ((IndexResponse) response.getResponse()).getId();
    }
    return id;
  }

  public String getFailedMessage(int max) {
    int count = 0;
    StringBuilder stringBuilder = new StringBuilder();
    for (Map.Entry<String, String> entry : failed.entrySet()) {
      if (StringUtils.indexOf(entry.getValue(), "VersionConflictEngineException") < 0) {
        stringBuilder.append(String.format("id:%s error:%s", entry.getKey(), entry.getValue()));
        count++;
        if (count >= max) {
          break;
        }
      }
    }
    return stringBuilder.toString();
  }
}
