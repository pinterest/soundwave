package com.pinterest.cmp.cmdb.elasticsearch;

import com.pinterest.cmp.cmdb.utils.ThrowingFunction;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.NotImplementedException;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class EsIterator<T> implements Iterator<T> {

  private ScrollableResponse<List<T>> response;
  private int currentListCursor;
  private ThrowingFunction<ScrollableResponse<List<T>>, ScrollableResponse<List<T>>>
      retrieveNextFunction;


  public EsIterator(ScrollableResponse<List<T>> response,
                    ThrowingFunction<ScrollableResponse<List<T>>, ScrollableResponse<List<T>>>
                        retrieveCallback) {
    Preconditions.checkNotNull(response);

    this.retrieveNextFunction = retrieveCallback;
    this.response = response;
    this.currentListCursor = 0;
  }

  public ScrollableResponse<List<T>> getResponse() {
    return response;
  }

  public void setResponse(ScrollableResponse<List<T>> response) {
    this.response = response;
  }

  public boolean hasNext() {
    boolean ret;
    if (response.isScrollToEnd()) {
      ret = currentListCursor < response.getValue().size();
    } else {
      if (currentListCursor >= response.getValue().size()) {
        try {
          response = this.retrieveNextFunction.apply(response);
          currentListCursor = 0;
          ret = currentListCursor < response.getValue().size();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      } else {
        ret = true;
      }
    }
    return ret;
  }

  public T next() {
    if (hasNext()) {
      return response.getValue().get(currentListCursor++);
    } else {
      throw new NoSuchElementException();
    }
  }

  public void remove() {
    throw new NotImplementedException("remove is not a supported action");
  }
}
