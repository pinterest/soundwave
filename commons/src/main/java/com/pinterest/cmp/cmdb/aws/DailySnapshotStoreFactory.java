package com.pinterest.cmp.cmdb.aws;

import org.joda.time.DateTime;

public interface DailySnapshotStoreFactory {

  DailySnapshotStore getDailyStore(DateTime time);
}
