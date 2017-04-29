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
package com.pinterest.soundwave.job.definitions;

import com.pinterest.StatsUtil;
import com.pinterest.soundwave.pinterest.EsInstanceStore;

import com.twitter.ostrich.stats.Stats;
import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.action.admin.indices.stats.IndexStats;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ElasticSearchHealthCheckJob implements Callable<Boolean> {

  private EsInstanceStore esInstanceStore = new EsInstanceStore();

  @Override
  public Boolean call() throws Exception {
    logNodeStats(esInstanceStore.getNodesStats());
    logIndexStats(esInstanceStore.getIndexStats());
    return true;
  }

  private void logNodeStats(Map<String, NodeStats> statsMap) {
    Map<String, String> tags = new HashMap<>();
    for (NodeStats stat : statsMap.values()) {
      tags.put("esnode", stat.getHostname());
      Stats.setGauge(StatsUtil.getStatsName("eshealth", "heapUsedPercent", tags),
          stat.getJvm().getMem().getHeapUsedPrecent());
      Stats.setGauge(StatsUtil.getStatsName("eshealth", "heapMaxMB", tags),
          stat.getJvm().getMem().getHeapMax().getMbFrac());
      Stats.setGauge(StatsUtil.getStatsName("eshealth", "heapUsedMB", tags),
          stat.getJvm().getMem().getHeapUsed().getMbFrac());
      Stats.setGauge(StatsUtil.getStatsName("eshealth", "upMinutes", tags),
          stat.getJvm().getUptime().getMinutesFrac());
      Stats.setGauge(StatsUtil.getStatsName("eshealth", "docCount", tags),
          stat.getIndices().getDocs().getCount());
    }
  }

  private void logIndexStats(Map<String, IndexStats> indexStatsMap) {
    for (IndexStats stat : indexStatsMap.values()) {
      String indexKey = "esindex_" + stat.getIndex();
      Stats.setGauge(StatsUtil.getStatsName(indexKey, "primaryDocCount"),
          stat.getPrimaries().getDocs().getCount());
      Stats.setGauge(StatsUtil.getStatsName(indexKey, "totalDocCount"),
          stat.getTotal().getDocs().getCount());
      Stats.setGauge(StatsUtil.getStatsName(indexKey, "shardsCount"),
          stat.getShards().length);
      Stats.setGauge(StatsUtil.getStatsName(indexKey, "primaryStoreSizeMB"),
          stat.getPrimaries().getStore().getSize().getMbFrac());
      Stats.setGauge(StatsUtil.getStatsName(indexKey, "totalStoreSizeMB"),
          stat.getTotal().getStore().getSize().getMbFrac());
    }
  }
}
