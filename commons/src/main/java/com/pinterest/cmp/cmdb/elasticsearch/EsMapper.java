package com.pinterest.cmp.cmdb.elasticsearch;

import com.pinterest.cmp.cmdb.bean.EsStoreMappingProperty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import org.elasticsearch.search.SearchHit;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EsMapper convert a SearchHit to an object by property
 */
public class EsMapper {

  private static ConcurrentHashMap<String, String[]> fieldsCache = new ConcurrentHashMap<>();

  public static Object getObject(SearchHit hit, Class objectClass) {
    return null;
  }

  /**
   * Get fields for a type. It extracts all JsonProperty fields
   * @param type
   * @return
   */
  public static String[] getIncludeFields(Class type) {
    if (!fieldsCache.containsKey(type.getTypeName())) {
      Reflections
          reflections =
          new Reflections(type.getCanonicalName(), new FieldAnnotationsScanner());
      List<String> ret = new ArrayList<>();
      for (Field field : reflections.getFieldsAnnotatedWith(JsonProperty.class)) {
        JsonProperty property = field.getAnnotation(JsonProperty.class);
        ret.add(property.value());
      }

      if (type.getSuperclass() != null) {
        ret.addAll(Arrays.asList(getIncludeFields(type.getSuperclass())));
      }

      fieldsCache.putIfAbsent(type.getTypeName(), ret.toArray(new String[ret.size()]));
    }
    return fieldsCache.get(type.getTypeName());
  }

  /**
   * Get the fields for a particular ES index. It uses JsonProperty field value if no specific
   * EsStoreMappingProperty is set
   * @param type
   * @param store
   * @return
   */
  public static String[] getIncludeFields(Class type, Class<? extends EsStore> store) {
    String key = String.format("%s_%s", type.getTypeName(), store.getTypeName());
    String[] ret = fieldsCache.get(key);
    if (ret == null) {
      List<String> fields = new ArrayList<>();
      for (Field field : ReflectionUtils.getAllFields(type)) {
        JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
        EsStoreMappingProperty
            storeSpecificProperty =
            field.getAnnotation(EsStoreMappingProperty.class);

        if ((jsonProperty == null && storeSpecificProperty == null)
            || (storeSpecificProperty != null && storeSpecificProperty.ignore())) {
          //Either has no Json Property or it is set to ignore
          continue;
        }

        if (storeSpecificProperty == null
            || (storeSpecificProperty.store() != store && !storeSpecificProperty.store()
                .isAssignableFrom(store))) {
          //No store set. Just use json property
          //Or the store class in store property is not the same as the current store or a
          // superclass for the store.
          fields.add(jsonProperty.value());
        } else {
          int idx = storeSpecificProperty.value().indexOf('.');
          if (idx > 0) {
            fields.add(storeSpecificProperty.value().substring(0, idx));
          } else {
            fields.add(storeSpecificProperty.value());
          }
        }
      }
      fields = ImmutableSet.copyOf(fields).asList();
      ret = new String[fields.size()];
      ret = fields.toArray(ret);
      fieldsCache.put(key, ret);
    }
    return ret;
  }
}

