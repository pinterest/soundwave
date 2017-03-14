package com.pinterest.cmp.cmdb.elasticsearch;


import com.pinterest.cmp.cmdb.bean.EsAwsStatus;
import com.pinterest.cmp.cmdb.bean.EsDailySnapshotInstance;
import com.pinterest.cmp.cmdb.bean.EsInstance;
import com.pinterest.cmp.cmdb.bean.EsQueryResult;
import com.pinterest.cmp.cmdb.pinterest.EsInstanceTags;
import com.pinterest.cmp.cmdb.pinterest.EsMetaData;
import com.pinterest.cmp.cmdb.pinterest.EsNameMetaData;

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
