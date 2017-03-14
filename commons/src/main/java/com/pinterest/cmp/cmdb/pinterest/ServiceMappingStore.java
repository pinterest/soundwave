package com.pinterest.cmp.cmdb.pinterest;

import java.util.List;

public interface ServiceMappingStore {

  List<EsServiceMapping> getServiceMappings() throws Exception;

  EsServiceMapping getServiceMappingByName(String name) throws Exception;

  long updateOrInsertServiceMapping(EsServiceMapping serviceMapping) throws Exception;

}
