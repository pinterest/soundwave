package com.pinterest.cmp.cmdb.aws;

import com.pinterest.cmp.cmdb.bean.EsDailySnapshotInstance;

import java.util.Iterator;
import java.util.List;

public interface DailySnapshotStore {

  EsDailySnapshotInstance getInstanceById(String instanceId) throws Exception;

  long updateOrInsert(EsDailySnapshotInstance instance) throws Exception;

  long update(EsDailySnapshotInstance instance) throws Exception;

  Iterator<EsDailySnapshotInstance> getSnapshotInstances() throws Exception;

  void bulkInsert(List<EsDailySnapshotInstance> instances) throws Exception;

  void bulkUpdate(List<EsDailySnapshotInstance> instances) throws Exception;
}
