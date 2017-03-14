package com.pinterest.cmp.cmdb.bean;

public class InvalidStateTransitionException extends Exception {

  public InvalidStateTransitionException(String message) {
    super(message);
  }
}
