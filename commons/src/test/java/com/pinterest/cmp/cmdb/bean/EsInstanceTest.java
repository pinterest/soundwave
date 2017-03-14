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
