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
