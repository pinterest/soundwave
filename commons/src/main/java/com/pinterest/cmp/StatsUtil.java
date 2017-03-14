package com.pinterest.cmp;

import com.pinterest.cmp.config.Configuration;

import java.util.Map;

public class StatsUtil {

  // Stats Constants
  private static final String STATS_SEPARATOR = "_";
  private static String
      SERVICE_NAME;

  static {
    try {
      SERVICE_NAME =
          Configuration.getProperties().getString("service_name", "unknown");
    } catch (Exception e) {
      SERVICE_NAME = "unknown";
    }
  }

  public static String getStatsName(String methodName, String statName) {
    StringBuilder stringBuilder = new StringBuilder(128);
    appendNames(stringBuilder, methodName, statName, null);
    return stringBuilder.toString();
  }

  public static String getStatsName(String methodName, String statName, Map<String, String> tags) {
    StringBuilder stringBuilder = new StringBuilder(128);
    appendNames(stringBuilder, methodName, statName, null);
    appendTags(stringBuilder, tags);
    return stringBuilder.toString();
  }

  public static String getStatsName(String methodName, String statName, StatsType type) {
    StringBuilder stringBuilder = new StringBuilder(128);
    appendNames(stringBuilder, methodName, statName, type);
    return stringBuilder.toString();
  }

  public static String getStatsName(String methodName, String statName, StatsType type,
                                    Map<String, String> tags) {
    StringBuilder stringBuilder = new StringBuilder(128);
    appendNames(stringBuilder, methodName, statName, type);
    appendTags(stringBuilder, tags);
    return stringBuilder.toString();
  }

  private static void appendNames(StringBuilder stringBuilder, String methodName, String statName,
                                  StatsType type) {
    stringBuilder.append(SERVICE_NAME);
    stringBuilder.append(STATS_SEPARATOR);
    stringBuilder.append(methodName);
    stringBuilder.append(STATS_SEPARATOR);
    stringBuilder.append(statName);
    if (type != null) {
      stringBuilder.append(STATS_SEPARATOR);
      stringBuilder.append(type.getName());
    }
  }

  private static void appendTags(StringBuilder stringBuilder, Map<String, String> tags) {
    if (tags == null || tags.size() == 0) {
      return;
    }

    for (Map.Entry<String, String> entry : tags.entrySet()) {
      stringBuilder.append(" ");
      stringBuilder.append(entry.getKey());
      stringBuilder.append("=");
      stringBuilder.append(entry.getValue());
    }
  }

  public static enum StatsType {
    SUCCESS("success"),
    FAILURE("failure"),
    REQUEST("request"),
    RETRY("retry"),
    TIME("time");

    private String methodName;

    private StatsType(String name) {
      this.methodName = name;
    }

    public String getName() {
      return this.methodName;
    }
  }
}
