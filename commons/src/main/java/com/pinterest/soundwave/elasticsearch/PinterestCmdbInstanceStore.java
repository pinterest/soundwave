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
package com.pinterest.soundwave.elasticsearch;

import com.pinterest.soundwave.pinterest.EsFacts;
import com.pinterest.soundwave.pinterest.EsFactsAndPackages;
import com.pinterest.soundwave.pinterest.EsInstanceTags;
import com.pinterest.soundwave.pinterest.EsPackages;

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
