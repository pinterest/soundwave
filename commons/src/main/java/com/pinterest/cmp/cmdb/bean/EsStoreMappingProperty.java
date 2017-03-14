package com.pinterest.cmp.cmdb.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EsStoreMappingProperty {

  String value() default "";

  Class store();

  boolean ignore() default false;
}
