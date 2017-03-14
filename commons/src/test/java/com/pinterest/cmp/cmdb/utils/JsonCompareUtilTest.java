package com.pinterest.cmp.cmdb.utils;

import com.pinterest.cmp.cmdb.bean.EsInstance;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.pinterest.cmp.cmdb.utils.JsonCompareUtil;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JsonCompareUtilTest {

  @Test
  public void findDiff() throws Exception {
    EsInstance es1 = new EsInstance();
    EsInstance es2 = new EsInstance();
    Assert.assertEquals(0, JsonCompareUtil.findDiff(es1, es2).size());

    es1.setId("abc");
    es2.setId("def");
    Assert.assertEquals(1, JsonCompareUtil.findDiff(es1, es2).size());

    DateTime now = DateTime.now();
    es1.setCreatedTime(now.toDate());
    es2.setCreatedTime(now.toDate());
    Assert.assertEquals(1, JsonCompareUtil.findDiff(es1, es2).size());

    es2.setCreatedTime(now.minus(1).toDate());
    Assert.assertEquals(2, JsonCompareUtil.findDiff(es1, es2).size());

    es1.setSecurityGroups(Arrays.asList("abc"));
    Assert.assertEquals(3, JsonCompareUtil.findDiff(es1, es2).size());
    es2.setSecurityGroups(Arrays.asList("abc"));
    Assert.assertEquals(2, JsonCompareUtil.findDiff(es1, es2).size());
    es2.setSecurityGroups(Arrays.asList("abc", "def"));
    Assert.assertEquals(3, JsonCompareUtil.findDiff(es1, es2).size());

    es1.setCloud(new HashMap<String, Object>());
    es1.getCloud().put("a", new HashMap<String, Object>());
    ((HashMap<String, Object>) es1.getCloud().get("a")).put("b", Arrays.asList("c"));
    Assert.assertEquals(4, JsonCompareUtil.findDiff(es1, es2).size());
    es2.setCloud(new HashMap<String, Object>());
    es2.getCloud().put("a", new HashMap<String, Object>());
    ((HashMap<String, Object>) es2.getCloud().get("a")).put("b", Arrays.asList("c"));

    Assert.assertEquals(3, JsonCompareUtil.findDiff(es1, es2).size());
    ((HashMap<String, Object>) es1.getCloud().get("a")).put("b", "d");
    Assert.assertEquals(4, JsonCompareUtil.findDiff(es1, es2).size());

    es1.getCloud().put("array", new String[]{"a"});
    es2.getCloud().put("array", new String[]{"a"});
    Assert.assertEquals(4, JsonCompareUtil.findDiff(es1, es2).size());
  }

  @Test
  public void TestDiff() {
    HashMap<String, Object> map1 = new HashMap<>();
    HashMap<String, Object> map2 = new HashMap<>();
    map1.put("bbbb", "cccc");
    map1.put("xxx", "aaa");
    map2.put("xxx", "aa");
    map2.put("cccc", "bbbb");
    map1.put("dict", ImmutableMap.builder().put("a", 1).put("b", 2)
        .put("em", ImmutableMap.builder().put("c", 3).build()).build());
    map2.put("dict", ImmutableMap.builder().put("a", 1).put("b", 3)
        .put("em", ImmutableMap.builder().put("c", 4).put("d", 5).build()).build());
    MapDifference diff = Maps.difference(map1, map2);
    Map diffMap = new HashMap();
    JsonCompareUtil.getDetailsDiff(map1, map2, diffMap, "");
    Assert.assertTrue(diffMap.containsKey("bbbb"));
    Assert.assertTrue(diffMap.containsKey("xxx"));
    Assert.assertTrue(diffMap.containsKey("cccc"));
    Assert.assertTrue(diffMap.containsKey("dict.b"));
    Assert.assertTrue(diffMap.containsKey("dict.em.c"));
    Assert.assertTrue(diffMap.containsKey("dict.em.d"));
    Assert.assertEquals(6, diffMap.size());
  }

}
