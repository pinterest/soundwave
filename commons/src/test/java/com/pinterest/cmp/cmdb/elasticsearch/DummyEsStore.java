package com.pinterest.cmp.cmdb.elasticsearch;

import com.pinterest.cmp.cmdb.elasticsearch.EsStore;

public class DummyEsStore extends EsStore {

  @Override
  public String getDocTypeName() {
    return "testDocType";
  }

  @Override
  public String getIndexName() {
    return "lida_aws_test";
  }
}
