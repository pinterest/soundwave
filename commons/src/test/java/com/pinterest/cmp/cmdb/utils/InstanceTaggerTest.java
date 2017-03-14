package com.pinterest.cmp.cmdb.utils;

import com.pinterest.cmp.cmdb.pinterest.EsServiceMapping;

import com.pinterest.cmp.cmdb.pinterest.InstanceTagger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

;

public class InstanceTaggerTest {

  @Test
  @Ignore
  public void findMapping() throws Exception {
    InstanceTagger tagger = InstanceTagger.getInstance();
    List<EsServiceMapping> mapping = tagger.findMapping("pinalyticsv2-a01-regionserver-00039ea0");
    Assert.assertNotNull(mapping);

    InstanceTagger.InstanceTags tags = tagger.getTags("cmp-pinshot-newkernel-0a0111e9");
    Assert.assertEquals("pinshot", tags.getServiceMappings()[0]);

  }

}
