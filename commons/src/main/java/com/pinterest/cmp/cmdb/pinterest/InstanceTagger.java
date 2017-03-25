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
package com.pinterest.cmp.cmdb.pinterest;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InstanceTagger {

  private static final InstanceTagger instance = new InstanceTagger();
  private final Lock lock = new ReentrantLock();
  private DateTime loaded = new DateTime(0);
  private List<EsServiceMapping> mappings = new ArrayList<>();
  private EsServiceMappingStore serviceMappingStore;

  private InstanceTagger() {
    serviceMappingStore = new EsServiceMappingStore();
  }

  public static InstanceTagger getInstance() {
    return instance;
  }

  public static boolean tagsAreEqual(String[] left, String[] right) {
    boolean ret = false;
    if (left != null & right != null) {
      if (left.length == right.length) {
        Arrays.sort(left);
        Arrays.sort(right);
        ret = Arrays.equals(left, right);
      }
    } else if (left == null) {
      ret = right == null || right.length == 0;
    } else if (right == null) {
      ret = left == null || left.length == 0;
    }

    return ret;
  }

  public List<EsServiceMapping> findMapping(String name) throws Exception {
    List<EsServiceMapping> ret = new ArrayList<>();

    if (StringUtils.isEmpty(name)) {
      return ret;
    }

    DateTime now = DateTime.now();
    Duration duration = new Duration(loaded, now);
    if (duration.getStandardMinutes() > 5) {
      boolean acquired = false;
      try {
        acquired = lock.tryLock();
        if (acquired) {
          duration = new Duration(loaded, now);
          if (duration.getStandardMinutes() > 5) {
            mappings = serviceMappingStore.getServiceMappings();
            loaded = DateTime.now();
          }
        }
      } catch (Exception ex) {
        //Log error
      } finally {
        if (acquired) {
          lock.unlock();
        }
      }
    }

    //This is not very efficient now as we have 691 match rules and requires 691+
    //regex match
    for (EsServiceMapping mapping : mappings) {
      if (mapping.matches(name)) {
        ret.add(mapping);
      }
    }
    return ret;
  }

  public InstanceTags getTags(String name) throws Exception {
    InstanceTags tags = new InstanceTags();
    String matchName = name;
    //All docker instances have been added a prefix cmp-. For ex:
    // pinshot-04215101
    // cmp-pinshot-newkernel-0a0111e9
    // To find out the tag, we need to get rid of the prefix.
    //
    if (StringUtils.isNotEmpty(name) && StringUtils.startsWithIgnoreCase(name, "cmp-")) {
      matchName = name.substring(4);
    }
    List<EsServiceMapping>
        mappings =
        InstanceTagger.getInstance().findMapping(matchName);
    HashSet<String> serviceMapping = new HashSet<>();
    HashSet<String> svcTag = new HashSet<>();
    HashSet<String> sysTag = new HashSet<>();
    HashSet<String> usageTag = new HashSet<>();

    for (EsServiceMapping mapping : mappings) {
      serviceMapping.add(mapping.getName());
      svcTag.add(mapping.getServiceTag());
      sysTag.add(mapping.getSysTag());
      if (!StringUtils.equals(mapping.getUsageTag(), "n/a")) {
        usageTag.add(mapping.getUsageTag());
      }
    }

    tags.setServiceMappings(serviceMapping.toArray(new String[serviceMapping.size()]));
    tags.setSvcTags(svcTag.toArray(new String[svcTag.size()]));
    tags.setSysTags(sysTag.toArray(new String[sysTag.size()]));
    tags.setUsageTags(usageTag.toArray(new String[usageTag.size()]));
    return tags;
  }

  public class InstanceTags {

    private String[] serviceMappings = new String[0];
    private String[] svcTags = new String[0];
    private String[] sysTags = new String[0];
    private String[] usageTags = new String[0];

    public String[] getUsageTags() {
      return usageTags;
    }

    public void setUsageTags(String[] usageTags) {
      this.usageTags = usageTags;
    }

    public String[] getSysTags() {
      return sysTags;
    }

    public void setSysTags(String[] sysTags) {
      this.sysTags = sysTags;
    }

    public String[] getSvcTags() {
      return svcTags;
    }

    public void setSvcTags(String[] svcTags) {
      this.svcTags = svcTags;
    }

    public String[] getServiceMappings() {
      return serviceMappings;
    }

    public void setServiceMappings(String[] serviceMappings) {
      this.serviceMappings = serviceMappings;
    }
  }
}
