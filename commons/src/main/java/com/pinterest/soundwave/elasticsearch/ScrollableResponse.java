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
