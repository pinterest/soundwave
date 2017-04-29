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


import com.pinterest.soundwave.bean.EsAwsStatus;
import com.pinterest.soundwave.bean.EsDailySnapshotInstance;
import com.pinterest.soundwave.bean.EsInstance;
import com.pinterest.soundwave.bean.EsQueryResult;
import com.pinterest.soundwave.pinterest.EsInstanceTags;
import com.pinterest.soundwave.pinterest.EsMetaData;
import com.pinterest.soundwave.pinterest.EsNameMetaData;

import com.amazonaws.regions.Region;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface CmdbInstanceStore {

  <E extends EsInstance> E getInstanceById(String instanceId) throws Exception;

  long updateOrInsertInstance(EsInstance instance) throws Exception;

  long update(EsInstance instance) throws Exception;

  Iterator<EsDailySnapshotInstance> getRunningAndRecentTerminatedInstances(int days)
      throws Exception;

  Iterator<EsInstance> getRunningInstances() throws Exception;

  Iterator<EsInstance> getRunningInstances(Region region) throws Exception;

  Iterator<EsInstance> getRunningInstances(Region region, String[] fields) throws Exception;

  Iterator<EsInstance> getRecentlyTerminatedInstances(Region region, int days) throws Exception;

  Iterator<EsInstanceTags> getRunningAndTerminatedInstanceTags(int terminatedDays) throws Exception;

  Iterator<EsAwsStatus> getRunningAndTerminatedAwsStatus(Region region, int days) throws Exception;

  void bulkInsert(List<EsInstance> instances) throws Exception;

  void bulkUpdate(List<EsInstance> instances) throws Exception;

  void bulkUpdateInstanceTags(List<EsInstanceTags> instances) throws Exception;

  void bulkUpdateAwsStatus(List<EsAwsStatus> awsStatuses) throws Exception;

  EsAwsStatus getAwsStatus(String instanceId) throws Exception;

  String checkStatus() throws Exception;

  Iterator<EsInstance> getInstanceCreatedBetween(Date start, Date end) throws Exception;

  Iterator<EsMetaData> getMetaData(String fieldName, String fieldValue) throws Exception;

  Iterator<EsQueryResult> query(String query, String[] includeFields) throws Exception;

  Iterator<EsQueryResult> getRunningInstancesWithFields(String[] includeFields) throws Exception;

  Iterator<EsNameMetaData> getMetaDataByName(String field, String name) throws Exception;

  Map<String, HashMap> getAggregations(List<String> aggregationParams) throws Exception;

  Class getInstanceClass();

}
