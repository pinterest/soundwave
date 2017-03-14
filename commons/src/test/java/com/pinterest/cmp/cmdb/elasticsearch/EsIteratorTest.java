package com.pinterest.cmp.cmdb.elasticsearch;

import com.pinterest.cmp.cmdb.elasticsearch.EsIterator;
import com.pinterest.cmp.cmdb.elasticsearch.ScrollableResponse;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EsIteratorTest {

  @Test
  public void noRetrieval() throws Exception {
    ScrollableResponse<List<Integer>> scrollableResponse = new ScrollableResponse<>();
    scrollableResponse.setValue(Arrays.asList(1, 2, 3, 4, 5));
    scrollableResponse.setScrollToEnd(true);

    EsIterator<Integer> l = new EsIterator<>(scrollableResponse, s -> scrollableResponse);
    int count = 0;
    ArrayList<Integer> result = new ArrayList<>();
    while (l.hasNext()) {
      result.add(l.next());
      count++;
    }
    Assert.assertArrayEquals(new int[]{1, 2, 3, 4, 5},
        ArrayUtils.toPrimitive(result.toArray(new Integer[0])));
  }

  @Test
  public void onRetrieval() throws Exception {
    ScrollableResponse<List<Integer>> scrollableResponse1 = new ScrollableResponse<>();
    scrollableResponse1.setValue(Arrays.asList(1, 2, 3, 4, 5));
    scrollableResponse1.setScrollToEnd(false);

    ScrollableResponse<List<Integer>> scrollableResponse2 = new ScrollableResponse<>();
    scrollableResponse2.setValue(Arrays.asList(1, 2, 3));
    scrollableResponse2.setScrollToEnd(true);

    EsIterator<Integer> l = new EsIterator<>(scrollableResponse1, s -> scrollableResponse2);
    int count = 0;
    ArrayList<Integer> result = new ArrayList<>();
    while (l.hasNext()) {
      result.add(l.next());
      count++;
    }

    Assert.assertArrayEquals(new int[]{1, 2, 3, 4, 5, 1, 2, 3},
        ArrayUtils.toPrimitive(result.toArray(new Integer[0])));
  }

}
