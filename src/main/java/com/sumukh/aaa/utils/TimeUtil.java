// src/main/java/com/sumukh/aaa/util/TimeUtil.java
package com.sumukh.aaa.utils;

import java.time.*;

public final class TimeUtil {
  private TimeUtil() {}

  public static String currentTime(String timeZoneId) {
    try {
      ZoneId zone = (timeZoneId == null || timeZoneId.isBlank())
          ? ZoneOffset.UTC
          : ZoneId.of(timeZoneId);
      ZonedDateTime now = ZonedDateTime.now(zone);
      return now.toString(); // ISO 8601 with zone offset
    } catch (Exception e) {
      return ZonedDateTime.now(ZoneOffset.UTC).toString();
    }
  }
}
