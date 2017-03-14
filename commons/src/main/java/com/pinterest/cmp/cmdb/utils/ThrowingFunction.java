package com.pinterest.cmp.cmdb.utils;

@FunctionalInterface
public interface ThrowingFunction<T, R> {

  R apply(T t) throws Exception;
}
