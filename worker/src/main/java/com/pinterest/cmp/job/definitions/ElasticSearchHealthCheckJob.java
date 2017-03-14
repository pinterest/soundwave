package com.pinterest.cmp.job.definitions;

import com.pinterest.cmp.StatsUtil;
import com.pinterest.cmp.cmdb.pinterest.EsInstanceStore;

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
