package com.pinterest.cmp;


import com.pinterest.cmp.cmdb.bean.EsInstance;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public abstract class UploadTagsGenerator {

  /**
   * Return a list of tags that need to be updated to the Ec2Instance.
   * The tags are either not in Ec2Instance tags or having different
   * values
   * @param ec2Instance
   * @param esInstance
   * @return A list of tags
   */
  public List<Tag> getUpdateTags(Instance ec2Instance, EsInstance esInstance) {
    Preconditions.checkNotNull(ec2Instance);
    Preconditions.checkNotNull(esInstance);
    List<Tag> updateTags = new ArrayList<>();

    List<Tag> currentEc2Tag = ec2Instance.getTags();
    List<Tag> esUploadTags = getUpdateTags(esInstance);

    for (Tag tag : esUploadTags) {
      boolean shouldUpdate = true;
      for (Tag ec2Tag : currentEc2Tag) {
        if (ec2Tag.getKey().equals(tag.getKey()) && ec2Tag.getValue().equals(tag.getValue())) {
          shouldUpdate = false;
          break;
        }
      }

      if (shouldUpdate) {
        updateTags.add(tag);
      }
    }

    return updateTags;

  }

  /**
   * Get all tags needed to be added to Ec2Instance
   * @param esInstance
   * @return A list of tags
   */
  public abstract List<Tag> getUpdateTags(EsInstance esInstance);
}
