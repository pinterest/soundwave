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

package com.pinterest.soundwave.utils;

import com.pinterest.soundwave.annotations.IgnoreAdaptation;
import com.pinterest.soundwave.annotations.NestedField;
import com.pinterest.soundwave.annotations.StringDate;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectAdapter {

  private static final Logger logger = LoggerFactory.getLogger(ObjectAdapter.class);
  private static ConcurrentHashMap<String, List<String>> fieldsCache = new ConcurrentHashMap<>();
  private static ConcurrentHashMap<Class, Map<String, Field>>
      classAllFieldsCache =
      new ConcurrentHashMap<>();

  /**
   * Gets an array of jsonFields
   *
   * @param classType class reference
   * @return cached fields
   */
  public static List<String> getFields(Class classType) {

    if (!fieldsCache.containsKey(classType.getTypeName())) {

      List<String> fields = new ArrayList<>();
      for (Field field : classType.getDeclaredFields()) {
        fields.add(field.getName());
      }
      fieldsCache.putIfAbsent(classType.getTypeName(), fields);
    }

    return fieldsCache.get(classType.getTypeName());
  }

  public static Map<String, Field> getAllFields(Class classType) {
    if (!classAllFieldsCache.containsKey(classType)) {
      Set<Field> objectFieldsSet = ReflectionUtils.getAllFields(classType);
      Map<String, Field> objectFieldsMap = new HashMap<>();
      for (Field f : objectFieldsSet) {
        objectFieldsMap.put(f.getName(), f);
      }
      classAllFieldsCache.put(classType, objectFieldsMap);
    }
    return classAllFieldsCache.get(classType);
  }

  public static <T1, T2> T2 getObject(T1 object, Class<T2> adaptedClass) {

    try {
      Map<String, Field> objectFieldsMap = getAllFields(object.getClass());

      T2 adaptedObject = (T2) ConstructorUtils.invokeConstructor(adaptedClass, null);
      List<String> target = getFields(adaptedClass);
      for (String field : target) {

        // get The field of the adapted object
        Field targetField = adaptedClass.getDeclaredField(field);
        targetField.setAccessible(true);

        if (targetField.isAnnotationPresent(NestedField.class)) {

          NestedField annotation = targetField.getDeclaredAnnotation(NestedField.class);
          String[] hierarchy = StringUtils.split(annotation.src(), ".");
          Field nestedField = objectFieldsMap.get(hierarchy[0]);
          nestedField.setAccessible(true);
          Object fieldValue = nestedField.get(object);

          for (int i = 1; i < hierarchy.length; i++) {
            nestedField = nestedField.getType().getDeclaredField(hierarchy[i]);
            nestedField.setAccessible(true);
            fieldValue = nestedField.get(fieldValue);
          }

          // Set the last level value from hierarchy
          targetField.set(adaptedObject, fieldValue);

        } else {

          // Non nested field process as normal
          Field sourceField;
          if (targetField.isAnnotationPresent(StringDate.class)) {

            // Process date fields
            sourceField = objectFieldsMap.get(field);
            sourceField.setAccessible(true);

            if (sourceField.get(object) != null) {

              // Value is not null
              DateTime time = new DateTime(sourceField.get(object), DateTimeZone.UTC);
              targetField.set(adaptedObject, time.toString());
            } else {

              targetField.set(adaptedObject, "");
            }


          } else if (targetField.isAnnotationPresent(IgnoreAdaptation.class)) {
            // Leave field as it is. no processing.
          } else {

            sourceField = objectFieldsMap.get(field);
            sourceField.setAccessible(true);

            targetField.set(adaptedObject, sourceField.get(object));
          }
        }
      }

      return adaptedObject;

    } catch (Exception e) {
      logger.error(ExceptionUtils.getRootCauseMessage(e));
      return null;
    }


  }


}
