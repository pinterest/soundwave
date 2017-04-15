package com.pinterest.soundwave.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this annotation to ignore this field during the processing of object adapter
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreAdaptation {

}
