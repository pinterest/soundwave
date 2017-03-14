package com.pinterest.cmp.job;

public interface OwnershipDecider {

  boolean isOwner();

  String getNodeName();
}
