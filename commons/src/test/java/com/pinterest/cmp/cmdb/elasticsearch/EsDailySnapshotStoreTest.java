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

import com.pinterest.cmp.cmdb.bean.EsDailySnapshotInstance;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

public class EsDailySnapshotStoreTest {

  @Test
  @Ignore
  public void ensureIndexInitialized() throws Exception {
    EsDailySnapshotStoreFactory factory = new EsDailySnapshotStoreFactory();
    EsDailySnapshotStore
        store =
        (EsDailySnapshotStore) factory.getDailyStore(DateTime.parse("2015-01-01"));
  }

  @Test
  @Ignore
  public void getInstanceById() throws Exception {
    EsDailySnapshotStoreFactory factory = new EsDailySnapshotStoreFactory();
    EsDailySnapshotStore
        store =
        (EsDailySnapshotStore) factory.getDailyStore(DateTime.parse("2016-07-14"));
    EsDailySnapshotInstance inst = store.getInstanceById("i-9909451f");
    Assert.assertEquals("i-9909451f", inst.getId());
    Assert.assertEquals("Growth", inst.getUsageTag()[0]);
    inst = store.getInstanceById("i-999c8b05");
    Assert.assertEquals("SRE", inst.getSysTag()[0]);
    Assert.assertEquals("vpc-app-production", inst.getSecurityGroups().get(0));

  }

  @Test
  @Ignore
  public void updateOrInsert() throws Exception {
    EsDailySnapshotStoreFactory factory = new EsDailySnapshotStoreFactory();
    EsDailySnapshotStore
        store =
        (EsDailySnapshotStore) factory.getDailyStore(DateTime.parse("2016-07-14"));
    EsDailySnapshotInstance inst = new EsDailySnapshotInstance();
    inst.setId("i-123456789");
    inst.setName("test123");
    inst.setLaunchTime(DateTime.now().toDate());
    inst.setServiceMappings(new String[]{"mapping"});
    store.updateOrInsert(inst);
  }

  @Test
  public void findDiff() throws Exception {
    EsDailySnapshotInstance inst1 = new EsDailySnapshotInstance();
    EsDailySnapshotInstance inst2 = new EsDailySnapshotInstance();
    Assert.assertEquals(0, inst1.findDiff(inst2).size());
    
    inst1.setName("a");
    Assert.assertTrue(inst1.findDiff(inst2).containsKey("name"));

    inst2.setName("b");
    Assert.assertEquals("b", (String) inst1.findDiff(inst2).get("name")[1]);

    inst1.setName("b");
    DateTime now = DateTime.now();
    inst1.setLaunchTime(now.toDate());
    inst2.setLaunchTime(now.toDate());
    Assert.assertEquals(0, inst1.findDiff(inst2).size());

    inst2.setLaunchTime(now.minus(1).toDate());
    Assert.assertEquals(inst2.getLaunchTime(), inst1.findDiff(inst2).get("launch_time")[1]);

    inst2.setLaunchTime(inst1.getLaunchTime());
    inst1.setServiceMappings(new String[]{"a"});
    inst2.setServiceMappings(new String[]{"a"});
    Assert.assertEquals(0, inst1.findDiff(inst2).size());

    inst2.setServiceMappings(new String[]{"b"});
    Map<String, Object[]> result = inst1.findDiff(inst2);
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(inst1.findDiff(inst2).containsKey("service_mapping"));
  }

}
