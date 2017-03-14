package com.pinterest.cmp.cmdb.elasticsearch;

public class ScrollableResponse<T> {

  private T value;
  private String continousToken;
  private boolean scrollToEnd;


  public ScrollableResponse() {

  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public String getContinousToken() {
    return continousToken;
  }

  public void setContinousToken(String continousToken) {
    this.continousToken = continousToken;
  }


  public boolean isScrollToEnd() {
    return scrollToEnd;
  }

  public void setScrollToEnd(boolean scrollToEnd) {
    this.scrollToEnd = scrollToEnd;
  }

}
