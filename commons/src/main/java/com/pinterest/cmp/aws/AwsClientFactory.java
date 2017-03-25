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
package com.pinterest.cmp.aws;

import com.pinterest.cmp.config.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory client to get various AWS service client
 */
public class AwsClientFactory {

  private static Logger logger = LoggerFactory.getLogger(AwsClientFactory.class);
  private static AWSCredentialsProvider credentialsProvider = null;

  private static synchronized AWSCredentialsProvider getCredentialProvider() {
    if (credentialsProvider == null) {
      boolean useInstanceProfile = Configuration.getProperties().getBoolean(
          "use_instance_profile", false);

      if (useInstanceProfile) {
        credentialsProvider = new InstanceProfileCredentialsProvider();
      } else {
        //If not specifies use Ec2 instance profile, use default chain
        credentialsProvider = new DefaultAWSCredentialsProviderChain();
      }
    }
    return credentialsProvider;
  }

  public static AmazonSQSClient createSQSClient() {
    return new AmazonSQSClient(getCredentialProvider());
  }

  public static AmazonSQSClient createSQSClient(Region region) {
    Preconditions.checkNotNull(region);
    AmazonSQSClient client = new AmazonSQSClient(getCredentialProvider());
    client.setRegion(region);
    return client;
  }

  public static AmazonEC2Client createEC2Client() {
    return new AmazonEC2Client(getCredentialProvider());
  }

  public static AmazonEC2Client createEC2Client(Region region) {
    Preconditions.checkNotNull(region);
    AmazonEC2Client client = new AmazonEC2Client(getCredentialProvider());
    client.setRegion(region);
    return client;
  }

  public static AmazonS3Client createS3Client() {
    return new AmazonS3Client(getCredentialProvider());
  }

  public static AmazonS3Client createS3Client(Region region) {
    Preconditions.checkNotNull(region);
    AmazonS3Client s3Client = new AmazonS3Client(getCredentialProvider());
    s3Client.setRegion(region);
    return s3Client;
  }
}
