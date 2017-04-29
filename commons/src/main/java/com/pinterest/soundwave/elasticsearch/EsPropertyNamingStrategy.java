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

import com.pinterest.soundwave.bean.EsStoreMappingProperty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class EsPropertyNamingStrategy extends PropertyNamingStrategy {

  private Map<String, String> fieldToJsonMapping = new HashMap<>();
  private Class effectiveType;

  public EsPropertyNamingStrategy(Class type, Class<? extends EsStore> store) {
    this.effectiveType = type;

    for (Field field : ReflectionUtils.getAllFields(type)) {
      JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
      EsStoreMappingProperty
          storeSpecificProperty =
          field.getAnnotation(EsStoreMappingProperty.class);

      if ((jsonProperty == null && storeSpecificProperty == null)
          || (storeSpecificProperty != null && storeSpecificProperty.ignore())) {
        continue;
      }

      if (storeSpecificProperty == null || storeSpecificProperty.store() != store) {
        fieldToJsonMapping.put(jsonProperty.value(), jsonProperty.value());
      } else if (storeSpecificProperty.value().indexOf('.') < 0) {
        fieldToJsonMapping.put(jsonProperty.value(), storeSpecificProperty.value());
      }
    }
  }

  @Override
  public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam,
                                            String defaultName) {

    if (ctorParam.getDeclaringClass() != effectiveType) {
      return fieldToJsonMapping
          .getOrDefault(defaultName,
              super.nameForConstructorParameter(config, ctorParam, defaultName));
    } else {
      return super.nameForConstructorParameter(config, ctorParam, defaultName);
    }
  }

  @Override
  public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
    if (field.getDeclaringClass() == this.effectiveType) {
      return fieldToJsonMapping
          .getOrDefault(defaultName, super.nameForField(config, field, defaultName));
    } else {
      return super.nameForField(config, field, defaultName);
    }
  }

  @Override
  public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method,
                                    String defaultName) {
    if (method.getDeclaringClass() == this.effectiveType) {
      return fieldToJsonMapping
          .getOrDefault(defaultName, super.nameForGetterMethod(config, method, defaultName));
    } else {
      return super.nameForGetterMethod(config, method, defaultName);
    }
  }

  @Override
  public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method,
                                    String defaultName) {
    if (method.getDeclaringClass() == this.effectiveType) {
      return fieldToJsonMapping
          .getOrDefault(defaultName, super.nameForSetterMethod(config, method, defaultName));
    } else {
      return super.nameForSetterMethod(config, method, defaultName);
    }
  }
}
