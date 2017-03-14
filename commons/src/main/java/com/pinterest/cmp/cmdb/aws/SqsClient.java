package com.pinterest.cmp.cmdb.aws;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class SqsClient {

  private static final Logger logger = LoggerFactory.getLogger(SqsClient.class);
  private static final Logger sqsEventLogger = LoggerFactory.getLogger("sqsEventLogger");
  private static final Logger
      messageProcessingLogger =
      LoggerFactory.getLogger("messageProcessingLog");

  private AmazonSQSClient client;
  private Ec2NotificationHandler handler;
  private ObjectMapper objectMapper;


  public SqsClient(Ec2NotificationHandler handler) {
    Preconditions.checkNotNull(handler);
    objectMapper =
        new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    this.handler = handler;
  }

  public boolean processMessage(Message msg) {
    try {
      MessageProcessingResult result = null;
      NotificationEvent event =
          objectMapper.readValue(msg.getBody(), NotificationEvent.class);

      logger.info("Receive event {} with state {} created at {}",
          event.getDetail().getInstanceId(),
          event.getDetail().getState(), event.getTime());

      sqsEventLogger.info(objectMapper.writeValueAsString(event));
      Map<String, String> attributes = msg.getAttributes();

      if (attributes.containsKey("SentTimestamp")) {
        DateTime sentTime = new DateTime(Long.parseLong(attributes.get("SentTimestamp")),
            DateTimeZone.UTC);
        event.setSqsSentTime(sentTime.toDate());
      }

      if (handler != null) {
        result = handler.processEvent(event);
        if (result != null) {
          messageProcessingLogger.info(objectMapper.writeValueAsString(result));
          return result.isSucceed();
        }
      }
    } catch (Exception e) {
      logger.warn("Error Process message:{}", ExceptionUtils.getRootCauseMessage(e));
    }
    return false;
  }

}
