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
package com.pinterest.soundwave.aws;

import com.pinterest.StatsUtil;
import com.pinterest.soundwave.bean.EsDailySnapshotInstance;
import com.pinterest.soundwave.elasticsearch.CmdbInstanceStore;
import com.pinterest.OperationStats;
import com.pinterest.soundwave.bean.EsInstance;
import com.pinterest.soundwave.bean.PinterestEsInstance;
import com.pinterest.soundwave.bean.State;
import com.pinterest.config.Configuration;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.google.common.base.Preconditions;
import com.twitter.ostrich.stats.Stats;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.apache.commons.lang.time.StopWatch;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Update Handler for SQS notification events about Ec2.
 * There are three type of events: running, terminiated and stopped.
 */
public final class Ec2InstanceUpdateHandler implements Ec2NotificationHandler {


  private static final Logger logger = LoggerFactory.getLogger(Ec2InstanceUpdateHandler.class);
  private AbstractEsInstanceFactory esInstanceFactory;
  private CmdbInstanceStore cmdbInstanceStore;
  private CloudInstanceStore cloudInstanceStore;
  private DailySnapshotStoreFactory dailySnapshotStoreFactory;
  private List<BiConsumer<EsInstance, MessageProcessingResult>>
      onCreateSuccessHandlers =
      new ArrayList<>();


  public Ec2InstanceUpdateHandler(CmdbInstanceStore cmdbStore, CloudInstanceStore cloudStore,
                                  DailySnapshotStoreFactory dailySnapshotStoreFactory) {
    this.cmdbInstanceStore = cmdbStore;
    this.cloudInstanceStore = cloudStore;
    this.dailySnapshotStoreFactory = dailySnapshotStoreFactory;
    try {
      String className = Configuration.getProperties().getString("instance_factory",
          "com.pinterest.cmp.soundwave.pinterest.PinterestEsInstanceFactory");
      if (className.isEmpty()) {
        logger.error("instance_factory is not set");
      } else {
        Class factoryClass = Class.forName(className);
        this.esInstanceFactory = (AbstractEsInstanceFactory) ConstructorUtils.invokeConstructor(
            factoryClass, null);
        this.esInstanceFactory.setCloudInstanceStore(this.cloudInstanceStore);
      }
    } catch (Exception ex) {
      logger.error("Class {} cannot be loaded error {}",
          Configuration.getProperties().getString("instance_factory"), ex.getLocalizedMessage());
    }
  }


  @Override
  public MessageProcessingResult processEvent(NotificationEvent event)
      throws Exception {
    Preconditions.checkArgument(event != null);

    String state = event.getDetail().getState();
    MessageProcessingResult result = null;
    //Processing event
    if (State.RUNNING.isState(state)) {
      OperationStats op = new OperationStats("ec2_notification", "running_event");
      //An instance is running. Ensure we have the record for it
      result = ensureInstanceCreated(event);
      if (result.isSucceed()
          || result.getError() == MessageProcessingResult.MessageProcessingError.NO_NAME_TAG
          || result.getError()
          == MessageProcessingResult.MessageProcessingError.QUOBLE_NAME_NOT_AVAILABLE) {
        op.succeed();
      } else {
        op.failed();
      }

    } else if (State.TERMINATED.isState(state)) {
      OperationStats op = new OperationStats("ec2_notification", "terminated_event");
      //An instance has been terminated.
      result = ensureInstanceTerminated(event);
      if (result.isSucceed()) {
        op.succeed();
      } else {
        op.failed();
      }
    } else if (State.STOPPED.isState(state)) {
      OperationStats op = new OperationStats("ec2_notification", "stopped_event");
      //An instance has been stopped
      result = ensureInstanceStopped(event);
      if (result.isSucceed()) {
        op.succeed();
      } else {
        op.failed();
      }

    } else {
      logger.warn("Unexpect event state {} on instance {}", event.getDetail().getState(),
          event.getDetail().getInstanceId());
    }
    return result;
  }

  @Override
  public void registerOnCreationSuccessEventHandler(
      BiConsumer<EsInstance, MessageProcessingResult> handler) {
    this.onCreateSuccessHandlers.add(handler);
  }

  private MessageProcessingResult ensureInstanceCreated(NotificationEvent event) throws Exception {

    String instanceId = event.getDetail().getInstanceId();
    StopWatch watch = new StopWatch();
    watch.start();
    MessageProcessingResult result = new MessageProcessingResult(event);
    result.setSucceed(true);
    try {
      //Get EC2 instance
      Instance inst = cloudInstanceStore.getInstance(instanceId);

      if (inst != null) {
        //Create the corresponding EsInstance
        EsInstance esInstance = esInstanceFactory.createFromEC2(inst);

        long version = cmdbInstanceStore.updateOrInsertInstance(esInstance);
        if (version == 0) {
          logger.info("Instance {} is created", instanceId);
          //Created in ES
          DateTime utcNow = DateTime.now(DateTimeZone.UTC);
          DateTime enqueueTime = new DateTime(event.getSqsSentTime(), DateTimeZone.UTC);

          int sinceEnqueued = Seconds.secondsBetween(enqueueTime, utcNow).getSeconds();
          int
              sinceLaunched =
              Seconds.secondsBetween(new DateTime(esInstance.getAwsLaunchTime()), utcNow)
                  .getSeconds();

          //First time instance created
          Stats.addMetric(StatsUtil.getStatsName("ec2_creation", "since_enqueued"),
              sinceEnqueued > 0 ? sinceEnqueued : 0);

          Stats.addMetric(StatsUtil.getStatsName("ec2_creation", "since_launched"),
              sinceLaunched > 0 ? sinceLaunched : 0);
        } else {
          logger.info("Instance {} is updated", instanceId);
        }

        logger.info("Create Instance {} in ElasticSearch", instanceId);
        onInstanceCreation(esInstance, result);
        Tag nameTag = AwsUtilities.getAwsTag(inst, "Name");
        if (nameTag == null) {
          result.setError(MessageProcessingResult.MessageProcessingError.NO_NAME_TAG);
        } else if (State.PENDING.name().equalsIgnoreCase(esInstance.getState())) {
          //Still pending. Put back to the queue and wait it into running
          result.setError(MessageProcessingResult.MessageProcessingError.STILL_PENDING);
        } else {
          onInstanceCreation(esInstance, result);
        }
        try {
          syncWithDailySnapshot(event, esInstance);
        } catch (Exception ex) {
          logger.error("Failed to sync with daily snapshot {} with error {}", instanceId,
              ex.getMessage());
        }

      } else {
        result.setError(MessageProcessingResult.MessageProcessingError.NOT_EXIST_IN_EC_2);
      }
      return result;
    } finally {
      watch.stop();
      result.setDuration(watch.getTime());
    }
  }

  private MessageProcessingResult ensureInstanceTerminated(NotificationEvent event)
      throws Exception {
    String instanceId = event.getDetail().getInstanceId();
    StopWatch watch = new StopWatch();
    watch.start();
    MessageProcessingResult result = new MessageProcessingResult(event);
    try {

      EsInstance esInstance = cmdbInstanceStore.getInstanceById(instanceId);
      if (esInstance == null) {
        logger.warn("Cannot find instance {}. Try to create it", instanceId);
        ensureInstanceCreated(event);
        esInstance = cmdbInstanceStore.getInstanceById(instanceId);
      }

      if (esInstance != null) {
        Date now = DateTime.now(DateTimeZone.UTC).toDate();
        esInstance.setState(State.TERMINATED.toString());
        esInstance.setUpdatedTime(now);
        esInstance.setTerminateTime(event.getTime());

        cmdbInstanceStore.update(esInstance);
        logger.info("Terminate Instance {}", instanceId);
        result.setSucceed(true);
        try {
          syncWithDailySnapshot(event, esInstance);
        } catch (Exception ex) {
          logger.error("Failed to sync with daily snapshot {} with error {}", instanceId,
              ex.getMessage());
        }
      } else {
        result.setError(MessageProcessingResult.MessageProcessingError.NOT_EXIST_IN_ES);
        logger.error("The instance {} doesn't exist and may be invalid", instanceId);
      }
      return result;
    } finally {
      watch.stop();
      result.setDuration(watch.getTime());
    }

  }

  private MessageProcessingResult ensureInstanceStopped(NotificationEvent event) throws Exception {
    String instanceId = event.getDetail().getInstanceId();
    StopWatch watch = new StopWatch();
    watch.start();
    MessageProcessingResult result = new MessageProcessingResult(event);
    try {
      EsInstance esInstance = cmdbInstanceStore.getInstanceById(instanceId);
      if (esInstance == null) {
        logger.warn("Cannot find instance {}. Try to create it", instanceId);
        ensureInstanceCreated(event);
        esInstance = cmdbInstanceStore.getInstanceById(instanceId);
      }

      //SQS event can be out of order or reconciliation is running in parallel.
      // Occaionaly, we see the instance has been terminated when receiving
      //stopping event. Only update the state if the instance is running. Running is the only
      //possible state to transit to stopped per:
      // http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-lifecycle.html
      if (esInstance != null) {

        if (esInstance.getState().equals(State.RUNNING.toString())) {
          Date now = DateTime.now(DateTimeZone.UTC).toDate();
          esInstance.setState(State.STOPPED.toString());
          esInstance.setUpdatedTime(now);
          cmdbInstanceStore.update(esInstance);
          logger.info("Stop Instance {} in ElasticSearch", instanceId);
        } else {
          logger
              .info("Instance {} is not in RUNNING but in {} in ElasticSearch, skip it", instanceId,
                  esInstance.getState());
        }
        result.setSucceed(true);
      } else {
        logger.error("The instance {} doesn't exist and cannot be created", instanceId);
      }
      return result;
    } finally {
      watch.stop();
      result.setDuration(watch.getTime());
    }
  }

  private void syncWithDailySnapshot(NotificationEvent notificationEvent, EsInstance esInstance)
      throws Exception {
    EsDailySnapshotInstance
        snapshotInstance =
        new EsDailySnapshotInstance((PinterestEsInstance) esInstance);
    String state = notificationEvent.getDetail().getState();
    //Processing event
    if (State.RUNNING.isState(state)) {
      DateTime
          snapshotTime =
          new DateTime(esInstance.getAwsLaunchTime()).toDateTime(DateTimeZone.UTC);
      DailySnapshotStore store = this.dailySnapshotStoreFactory.getDailyStore(snapshotTime);
      store.updateOrInsert(snapshotInstance);
    } else {
      DateTime
          snapshotTime =
          new DateTime(notificationEvent.getTime()).toDateTime(DateTimeZone.UTC);
      DailySnapshotStore store = this.dailySnapshotStoreFactory.getDailyStore(snapshotTime);
      EsDailySnapshotInstance inst = store.getInstanceById(esInstance.getId());
      if (inst == null) {
        store.updateOrInsert(snapshotInstance);
      } else {
        store.update(snapshotInstance);
      }
    }
  }

  private void onInstanceCreation(EsInstance instance, MessageProcessingResult result) {
    for (BiConsumer<EsInstance, MessageProcessingResult> consumer : this.onCreateSuccessHandlers) {
      try {
        consumer.accept(instance, result);
      } catch (Exception ex) {
        logger.info("onCreateSuccessHandler got error {}", ExceptionUtils.getRootCauseMessage(ex));
      }
    }
  }

}
