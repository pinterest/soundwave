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
package com.pinterest.soundwave.utils;

import com.pinterest.soundwave.pinterest.EsServiceMapping;
import com.pinterest.soundwave.pinterest.InstanceTagger;

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
