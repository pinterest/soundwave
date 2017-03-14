package com.pinterest.cmp.cmdb.elasticsearch;

import com.pinterest.cmp.cmdb.pinterest.EsServiceMapping;

import com.pinterest.cmp.cmdb.pinterest.EsServiceMappingStore;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class ESServiceMappingStoreTest {

  @Test
  @Ignore
  public void getServiceMappings() throws Exception {
    EsServiceMappingStore store = new EsServiceMappingStore();
    List<EsServiceMapping> mappings = store.getServiceMappings();
    Assert.assertTrue(mappings.size() > 0);
  }

}
