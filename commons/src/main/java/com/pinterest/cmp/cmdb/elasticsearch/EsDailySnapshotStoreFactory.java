package com.pinterest.cmp.cmdb.elasticsearch;

import com.pinterest.cmp.cmdb.aws.DailySnapshotStore;
import com.pinterest.cmp.cmdb.aws.DailySnapshotStoreFactory;

import org.joda.time.DateTime;

import java.util.concurrent.ConcurrentHashMap;

public class EsDailySnapshotStoreFactory implements DailySnapshotStoreFactory {

  private static ConcurrentHashMap<String, EsDailySnapshotStore>
      s_Cache =
      new ConcurrentHashMap<>();

  @Override
  public DailySnapshotStore getDailyStore(DateTime day) {
    String key = day.toString("yyyy-MM-dd");
    EsDailySnapshotStore ret = s_Cache.get(key);
    if (ret == null) {
      ret = new EsDailySnapshotStore(day);
      s_Cache.put(key, ret);
    }
    return ret;
  }
}
