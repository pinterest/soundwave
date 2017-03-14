package com.pinterest.cmp.cmdb.elasticsearch;


import com.pinterest.cmp.cmdb.bean.EsInstanceCountRecord;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

public interface InstanceCounterStore {
  void bulkInsert(List<EsInstanceCountRecord> records) throws Exception;

  Iterator<EsInstanceCountRecord> getCountRecordsByDate(Date date) throws Exception;
}
