package com.pinterest.cmp.cmdb.aws;

/**
 * The exception indicates that there is no name tag on
 * the EC2 instance.
 */
public class MissingNameTagException extends Exception {

  public MissingNameTagException(String message) {
    super(message);
  }
}
