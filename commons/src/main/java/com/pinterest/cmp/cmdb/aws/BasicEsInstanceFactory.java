package com.pinterest.cmp.cmdb.aws;

import com.pinterest.cmp.cmdb.bean.EsInstance;

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
