package com.pinterest.cmp.job;

import com.pinterest.cmp.aws.AwsClientFactory;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class SqsTriggeredJobExecutor implements JobExecutor {

  private static final Logger logger = LoggerFactory.getLogger(SqsTriggeredJobExecutor.class);
  private final ScheduledExecutorService execService = Executors.newSingleThreadScheduledExecutor();
  private String queueUrl;
  private int retrieveIntervalSec;
  private Function<Message, Boolean> consumer;
  private AmazonSQSClient client;

  private int batchSize = 10;

  private int minimumReprocessingDuration = 5;


  public SqsTriggeredJobExecutor(String queueName, int retrieveIntervalInSec,
                                 Function<Message, Boolean> consumer) {
    this.queueUrl = queueName;
    this.retrieveIntervalSec = retrieveIntervalInSec;
    this.consumer = consumer;
    this.client = AwsClientFactory.createSQSClient();
  }


  public AmazonSQSClient getClient() {
    return client;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    Preconditions.checkArgument(batchSize > 0 && batchSize < 11);
    this.batchSize = batchSize;
  }

  public int getMinimumReprocessingDuration() {
    return minimumReprocessingDuration;
  }

  public void setMinimumReprocessingDuration(int minimumReprocessingDuration) {
    this.minimumReprocessingDuration = minimumReprocessingDuration;
  }

  @Override
  public void execute() {
    execService
        .scheduleAtFixedRate(() -> pullUntilNoMessages(), 0, retrieveIntervalSec, TimeUnit.SECONDS);
  }

  private void pullUntilNoMessages() {
    //10 is the maximum supported number of messages
    ReceiveMessageRequest
        request =
        new ReceiveMessageRequest().withMaxNumberOfMessages(this.batchSize)
            .withQueueUrl(queueUrl);
    request.withAttributeNames("All");

    request.setWaitTimeSeconds(10); //Long polling
    boolean processedMessage;
    do {
      processedMessage = false;

      List<Message> messages = new ArrayList<>(this.batchSize);
      try {
        logger.info("Receiving Messages");
        messages = this.getClient().receiveMessage(request).getMessages();
        logger.info("Finish receiving messages");
      } catch (Exception ex) {
        logger.error(ExceptionUtils.getRootCauseMessage(ex));
        logger.error(ExceptionUtils.getFullStackTrace(ex));
      }

      DateTime now = DateTime.now(DateTimeZone.UTC);
      for (Message msg : messages) {
        try {
          boolean completeProcessing = consumer.apply(msg);
          String sentTimeStr = msg.getAttributes().get("SentTimestamp");
          int eventLifeTime = 0;
          if (StringUtils.isNotEmpty(sentTimeStr)) {
            DateTime sentTime = new DateTime(Long.parseLong(sentTimeStr));
            eventLifeTime = new Period(sentTime, now).toStandardSeconds().getSeconds();
          }

          if (completeProcessing || eventLifeTime > 3600) {
            deleteMessage(msg);
          } else {
            //Fail to process. This can be common because some info.
            //Retry later with expotential back off
            this.changeMessageVisibility(msg,
                Math.max(this.minimumReprocessingDuration, eventLifeTime));
          }
          processedMessage = true;
        } catch (Exception ex) {
          logger.error(ExceptionUtils.getRootCauseMessage(ex));
          logger.error(ExceptionUtils.getFullStackTrace(ex));
        }
      }
    } while (processedMessage);
  }

  public void deleteMessage(Message msg) {
    DeleteMessageRequest
        request =
        new DeleteMessageRequest()
            .withQueueUrl(this.queueUrl)
            .withReceiptHandle(msg.getReceiptHandle());
    this.getClient().deleteMessage(request);
  }

  public void changeMessageVisibility(Message msg, int value) {
    logger.info("Change visibility to {} seconds", value);
    if (value > 36000) {
      value = 36000;
    }
    ChangeMessageVisibilityRequest
        request =
        new ChangeMessageVisibilityRequest()
            .withQueueUrl(this.queueUrl)
            .withReceiptHandle(msg.getReceiptHandle()).withVisibilityTimeout(value);
    this.getClient().changeMessageVisibility(request);
  }
}
