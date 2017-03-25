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
package com.pinterest.cmp.cmdb.bean;

import com.pinterest.cmp.cmdb.bean.EsInstance;
import com.pinterest.cmp.cmdb.bean.InvalidStateTransitionException;
import com.pinterest.cmp.cmdb.bean.State;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class EsInstanceTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void setState() throws Exception {
    EsInstance inst = new EsInstance();
    inst.setState(State.RUNNING.toString());
    Assert.assertEquals(inst.getState(), State.RUNNING.toString());
    inst.setState(State.RUNNING.toString());
    Assert.assertEquals(inst.getState(), State.RUNNING.toString());

    inst.setState(State.STOPPED.toString());
    Assert.assertEquals(inst.getState(), State.STOPPED.toString());

    inst.setState(State.RUNNING.toString());
    Assert.assertEquals(inst.getState(), State.RUNNING.toString());

    inst.setState(State.TERMINATED.toString());
    Assert.assertEquals(inst.getState(), State.TERMINATED.toString());

    thrown.expect(InvalidStateTransitionException.class);
    inst.setState(State.RUNNING.toString());

  }

}
