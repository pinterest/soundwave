package com.pinterest.cmp;

import com.pinterest.cmp.job.OwnershipDecider;

/**
 * A dummy ownership decider that always return true
 */
public class DummyOwnershipDecider implements OwnershipDecider {

  @Override
  public boolean isOwner() {
    return true;
  }

  @Override
  public String getNodeName() {
    return "dummy";
  }
}
