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
package com.pinterest.soundwave.resources;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class DailySnapshotTest {

  @Test
  @Ignore
  public void getDailySnapshot() throws Exception {
    DailySnapshot snapshot = new DailySnapshot();
    Response resp = snapshot.getDailySnapshot("2016-12-12");
    Assert.assertNotNull(resp);
  }

}
