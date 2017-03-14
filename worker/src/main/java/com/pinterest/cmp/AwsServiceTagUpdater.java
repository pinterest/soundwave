package com.pinterest.cmp;

import com.pinterest.cmp.cmdb.aws.CloudInstanceStore;
import com.pinterest.cmp.cmdb.aws.Ec2InstanceStore;
import com.pinterest.cmp.cmdb.bean.EsInstance;
import com.pinterest.cmp.config.Configuration;

import com.amazonaws.services.ec2.model.Tag;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public final class AwsServiceTagUpdater {

  private static final Logger logger = LoggerFactory.getLogger(AwsServiceTagUpdater.class);
  private static AwsServiceTagUpdater s_Instance = new AwsServiceTagUpdater(new Ec2InstanceStore());

  private CloudInstanceStore cloudStore;
  private UploadTagsGenerator tagsGenerator;

  private AwsServiceTagUpdater(CloudInstanceStore store) {
    Preconditions.checkNotNull(store);
    this.cloudStore = store;
    String
        tagsGeneratorClass =
        Configuration.getProperties()
            .getString("aws_tag_generator", "com.pinterest.cmp.BasicUploadTagsGenerator");
    try {
      this.tagsGenerator =
          (UploadTagsGenerator) ConstructorUtils
              .invokeConstructor(Class.forName(tagsGeneratorClass), null);
    } catch (Exception ex) {

    }
  }

  public static AwsServiceTagUpdater getInstance() {
    return s_Instance;
  }

  public UploadTagsGenerator getTagsGenerator() {
    return tagsGenerator;
  }

  public void updateTags(EsInstance inst) {
    List<Tag> updateTags = this.getTagsGenerator().getUpdateTags(inst);
    try {
      this.cloudStore.setTagsForInstances(Arrays.asList(inst.getId()), updateTags);
    } catch (Exception ex) {
      logger.warn("Faile to update tags for instance {}", inst.getId());
    }
  }


}
