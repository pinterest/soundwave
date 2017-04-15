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
package com.pinterest.soundwave.elasticsearch;

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
