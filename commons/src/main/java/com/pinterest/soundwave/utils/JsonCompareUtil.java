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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Preconditions;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * An utility to compare JsonProperty field on two objects
 */
public final class JsonCompareUtil {

  public static final Logger logger = LoggerFactory.getLogger(JsonCompareUtil.class);

  public static final ObjectMapper DumpMapper = new ObjectMapper().configure(
      SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public static <E> Map<String, Object[]> findDiff(E inst1, E inst2) throws Exception {
    return JsonCompareUtil.findDiff(inst1, inst2, new HashSet<String>());
  }

  public static <E> Map<String, Object[]> findDiff(E inst1, E inst2, Set<String> excludedFields)
      throws Exception {
    Preconditions.checkNotNull(inst1);
    Preconditions.checkNotNull(inst2);

    HashMap<String, Object[]> diff = new HashMap<>();
    //Compare all fields with @JsonProperty
    for (Field f : FieldUtils.getFieldsWithAnnotation(inst1.getClass(), JsonProperty.class)) {
      String name = f.getAnnotation(JsonProperty.class).value();
      if (excludedFields.contains(name)) {
        continue;
      }
      f.setAccessible(true);
      Object left = f.get(inst1);
      Object right = f.get(inst2);
      if (left == null && right == null) {
        continue;
      }

      if (f.getType().isArray()) {
        if (!Arrays.equals((Object[]) left, (Object[]) right)) {
          logger.info("Found diff on property {}", name);
          diff.put(name, new Object[]{left, right});
        }
      } else if (f.getType() == Map.class) {
        Map<String, Object[]> details = new HashMap<>();
        getDetailsDiff((Map) left, (Map) right, details, name);
        for (Map.Entry<String, Object[]> entry : details.entrySet()) {
          diff.put(entry.getKey(), entry.getValue());
        }
      } else {
        if (left == null || !left.equals(right)) {
          logger.info("Found diff on property {}", name);
          diff.put(name, new Object[]{left, right});
        }
      }
    }
    return diff;
  }

  public static void getDetailsDiff(Map left, Map right, Map<String, Object[]> diffs,
                                    String prefix) {
    MapDifference difference = Maps.difference(left, right);
    for (Object object : difference.entriesOnlyOnLeft().entrySet()) {
      Map.Entry entry = (Map.Entry) object;
      diffs.put(StringUtils.isEmpty(prefix) ? entry.getKey().toString()
                                            : prefix + "." + entry.getKey().toString(),
          new Object[]{entry.getValue(), null});
    }

    for (Object object : difference.entriesOnlyOnRight().entrySet()) {
      Map.Entry entry = (Map.Entry) object;
      diffs.put(StringUtils.isEmpty(prefix) ? entry.getKey().toString()
                                            : prefix + "." + entry.getKey().toString(),
          new Object[]{null, entry.getValue()});
    }

    for (Object object : difference.entriesDiffering().entrySet()) {
      Map.Entry entry = (Map.Entry) object;
      MapDifference.ValueDifference valueDiff = (MapDifference.ValueDifference) entry.getValue();
      if (valueDiff.leftValue() == null) {
        diffs.put(StringUtils.isEmpty(prefix) ? entry.getKey().toString()
                                              : prefix + "." + entry.getKey().toString(),
            new Object[]{null, valueDiff.rightValue()});
      } else if (valueDiff.rightValue() == null) {
        diffs.put(StringUtils.isEmpty(prefix) ? entry.getKey().toString()
                                              : prefix + "." + entry.getKey().toString(),
            new Object[]{valueDiff.leftValue(), null});

      } else if (valueDiff.leftValue() instanceof Map) {
        String newPrefix = StringUtils.isEmpty(prefix) ? entry.getKey().toString()
                                                       : prefix + "." + entry.getKey().toString();
        getDetailsDiff((Map) valueDiff.leftValue(), (Map) valueDiff.rightValue(), diffs,
            newPrefix);
      } else if (valueDiff.leftValue().getClass().isArray()) {
        //Guava uses equals array that doesn't give the right result. Use Arrays.equals
        if (!Arrays.equals((Object[]) valueDiff.leftValue(), (Object[]) valueDiff.rightValue())) {
          diffs.put(StringUtils.isEmpty(prefix) ? entry.getKey().toString()
                                                : prefix + "." + entry.getKey().toString(),
              new Object[]{valueDiff.leftValue(), valueDiff.rightValue()});
        }
      } else {
        diffs.put(StringUtils.isEmpty(prefix) ? entry.getKey().toString()
                                              : prefix + "." + entry.getKey().toString(),
            new Object[]{valueDiff.leftValue(), valueDiff.rightValue()});
      }
    }
  }
}
