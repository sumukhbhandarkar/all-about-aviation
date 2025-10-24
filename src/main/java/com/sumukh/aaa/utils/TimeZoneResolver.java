package com.sumukh.aaa.utils;

import java.time.ZoneId;
import java.util.Locale;

public final class TimeZoneResolver {
  private static Object engine; // lazy; avoid hard dep if you didn't add Timeshape

  private TimeZoneResolver(){}

  public static String resolve(String input, double lat, double lon) {
    // 1) If user provided something, try as-is
    if (input != null && !input.isBlank()) {
      String tz = input.trim();
      if (tz.toUpperCase(Locale.ROOT).startsWith("UTC")) tz = tz.substring(3);
      try { return ZoneId.of(tz).getId(); } catch (Exception ignored) {}
      throw new IllegalArgumentException("Invalid time zone: " + input);
    }

    // 2) Try Timeshape if available on classpath
    try {
      if (engine == null) {
        Class<?> engClass = Class.forName("net.iakovlev.timeshape.TimeZoneEngine");
        engine = engClass.getMethod("initialize").invoke(null);
      }
      var maybeZone = engine.getClass().getMethod("query", double.class, double.class)
              .invoke(engine, lat, lon);
      if (maybeZone != null) {
        var zoneId = (java.util.Optional<?>) maybeZone;
        if (zoneId.isPresent()) return ((java.time.ZoneId) zoneId.get()).getId();
      }
    } catch (ClassNotFoundException cnf) {
      // Timeshape not on classpath — continue to fallback
    } catch (Exception e) {
      // Timeshape present but failed — continue to fallback
    }

    // 3) Fallbacks: quick India box -> Asia/Kolkata, else UTC
    if (lat >= 6 && lat <= 37.5 && lon >= 68 && lon <= 97.5) {
      return "Asia/Kolkata"; // covers DEL/BLR/etc.
    }
    return "UTC";
  }
}
