package com.sumukh.aaa.utils;

import net.iakovlev.timeshape.TimeZoneEngine;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;

public final class TimeZoneResolver {
  private static final TimeZoneEngine ENGINE = TimeZoneEngine.initialize();
  private TimeZoneResolver(){}

  public static String resolve(String input, double lat, double lon) {
    // 1) If provided, try to parse as IANA or numeric offset like "+05:30" or "UTC+05:30"
    if (input != null && !input.isBlank()) {
      String tz = input.trim();
      // Normalize "UTC+05:30" -> "+05:30"
      if (tz.toUpperCase(Locale.ROOT).startsWith("UTC")) tz = tz.substring(3);
      // Small friendly mapping (avoid ambiguous abbreviations generally)
      if (tz.equalsIgnoreCase("IST")) tz = "Asia/Kolkata";

      try { return ZoneId.of(tz).getId(); } catch (Exception ignored) {}

      // Try parsing as pure offset without leading '+'
      try { return ZoneOffset.of(tz).getId(); } catch (Exception ignored) {}

      throw new IllegalArgumentException("Invalid time zone: " + input);
    }

    // 2) Auto-detect from coordinates
    Optional<ZoneId> zone = ENGINE.query(lat, lon);
    if (zone.isPresent()) return zone.get().getId();

    // 3) Fallback (very rare)
    return "UTC";
  }
}
