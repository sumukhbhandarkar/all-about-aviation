package com.sumukh.aaa.utils;

public final class Normalizers {
  private Normalizers() {} // utility class

  /**
   * Normalize an IATA code to uppercase and validate length (3 letters).
   */
  public static String iata(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("IATA code cannot be blank");
    }
    String code = raw.trim().toUpperCase();
    if (!code.matches("^[A-Z]{3}$")) {
      throw new IllegalArgumentException("Invalid IATA code: " + raw);
    }
    return code;
  }
}
