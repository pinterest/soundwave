package com.pinterest.cmp.cmdb.aws;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public final class AwsUtilities {
  public static Tag getAwsTag(Instance awsInstance, String tagName) {
    List<Tag> tags = awsInstance.getTags();
    java.util.Optional<Tag> tag =
        tags.stream().filter(t -> StringUtils.equals(t.getKey(), tagName)).findFirst();
    return tag.isPresent() ? tag.get() : null;
  }
}
