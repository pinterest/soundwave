package com.pinterest.cmp.cmdb.elasticsearch;

import com.pinterest.cmp.cmdb.pinterest.EsFacts;
import com.pinterest.cmp.cmdb.pinterest.EsFactsAndPackages;
import com.pinterest.cmp.cmdb.pinterest.EsInstanceTags;
import com.pinterest.cmp.cmdb.pinterest.EsPackages;

import java.util.Iterator;
import java.util.List;

public interface PinterestCmdbInstanceStore {

  EsFactsAndPackages getFactsAndPackagesById(String instanceId) throws Exception;

  long updateOrInsertFactsAndPackages(EsFactsAndPackages factsAndPackages) throws Exception;

  EsFacts getFactsById(String instanceId) throws Exception;

  long updateFacts(EsFacts facts) throws Exception;

  EsPackages getPackagesById(String instanceId) throws Exception;

  long updatePackages(EsPackages pkgs) throws Exception;

  Iterator<EsInstanceTags> getMissingServiceMappingInstances() throws Exception;

  List<String> getDeployments() throws Exception;

  List<String> getNodePools() throws Exception;

}
