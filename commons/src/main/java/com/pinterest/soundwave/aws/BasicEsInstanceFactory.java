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

import com.pinterest.soundwave.bean.EsInstance;

import com.amazonaws.services.ec2.model.Instance;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

public final class BasicEsInstanceFactory extends AbstractEsInstanceFactory{

  @Override
  public EsInstance createFromEC2(Instance awsInstance) throws Exception {
    Preconditions.checkNotNull(awsInstance);

    EsInstance esInstance = new EsInstance();

    esInstance.setId(awsInstance.getInstanceId());
    esInstance.setState(awsInstance.getState().getName());
    esInstance.setLocation(awsInstance.getPlacement().getAvailabilityZone());

    //Region=location-last char. This is what CMDBV1 and people on internet do.
    //There should be a better way. Right now, keep as what it is
    esInstance.setRegion(
        esInstance.getLocation().substring(0, esInstance.getLocation().length() - 1));
    esInstance.setAwsLaunchTime(awsInstance.getLaunchTime());
    esInstance.setSubnetId(awsInstance.getSubnetId());
    esInstance.setVpcId(awsInstance.getVpcId());


    //Convert AWS instance to a map of property bags and save it.
    esInstance.getCloud()
        .put("aws", getAwsInstanceProperties(awsInstance));

    Date utcNow = DateTime.now(DateTimeZone.UTC).toDate();
    esInstance.setCreatedTime(utcNow);
    esInstance.setUpdatedTime(utcNow);

    return esInstance;
  }
}
