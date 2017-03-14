package com.pinterest.cmp;

import com.pinterest.cmp.cmdb.bean.EsInstance;

import com.amazonaws.services.ec2.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class BasicUploadTagsGenerator extends UploadTagsGenerator {

  @Override
  public List<Tag> getUpdateTags(EsInstance esInstance) {
    return new ArrayList<>();
  }
}
