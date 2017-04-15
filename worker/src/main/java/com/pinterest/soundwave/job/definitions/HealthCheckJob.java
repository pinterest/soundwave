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
package com.pinterest.soundwave.job.definitions;

import com.pinterest.StatsUtil;
import com.pinterest.aws.AwsClientFactory;
import com.pinterest.config.Configuration;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.twitter.ostrich.stats.Stats;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

public class HealthCheckJob implements Callable<Boolean> {

  private static final Logger logger = LoggerFactory.getLogger(HealthCheckJob.class);
  private static final String QUEUELENGTHATTR = "ApproximateNumberOfMessages";
  private static final String QUEUEINVISIBLEATTR = "ApproximateNumberOfMessagesNotVisible";
  private final AmazonSQSClient sqsClient;
  private final String queueUrl;

  public HealthCheckJob() {
    sqsClient = AwsClientFactory.createSQSClient(Region.getRegion(Regions.US_EAST_1));
    queueUrl =
        Configuration.getProperties().getString("update_queue");
  }

  @Override
  public Boolean call() throws Exception {
    checkQueueLength();
    return true;
  }

  private void checkQueueLength() {
    try {
      GetQueueAttributesResult
          result =
          sqsClient.getQueueAttributes(queueUrl, Arrays.asList(QUEUELENGTHATTR,
              QUEUEINVISIBLEATTR));
      Map<String, String> attrs = result.getAttributes();

      if (attrs.containsKey(QUEUELENGTHATTR)) {
        Stats.addMetric(StatsUtil.getStatsName("healthcheck", "ec2queue_length"),
            Integer.parseInt(attrs.get(QUEUELENGTHATTR)));
        logger.info("Ec2 queue length is {}", attrs.get(QUEUELENGTHATTR));
      }

      if (attrs.containsKey(QUEUEINVISIBLEATTR)) {
        Stats.addMetric(StatsUtil.getStatsName("healthcheck", "ec2queue_in_processing"),
            Integer.parseInt(attrs.get("ApproximateNumberOfMessagesNotVisible")));
        logger.info("Ec2 queue in processing length is {}", attrs.get(QUEUEINVISIBLEATTR));
      }

    } catch (Exception ex) {
      logger.warn(ExceptionUtils.getRootCauseMessage(ex));
      logger.warn(ExceptionUtils.getFullStackTrace(ex));
    }

  }
}
