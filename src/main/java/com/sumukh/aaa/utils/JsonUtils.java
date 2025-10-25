// JsonUtils.java
package com.sumukh.aaa.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class JsonUtils {
  private static final ObjectMapper PRETTY = new ObjectMapper()
      .enable(SerializationFeature.INDENT_OUTPUT);

  private static final ObjectMapper STRICT = new ObjectMapper(); // for validation

  private JsonUtils(){}

  /** Validates it's JSON, returns canonical pretty string. */
  public static String toPretty(String raw) {
    if (raw == null || raw.isBlank()) return null;
    try {
      var node = STRICT.readTree(raw);      // validation + parse
      return PRETTY.writeValueAsString(node); // pretty
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("seatLayout must be valid JSON: " + e.getOriginalMessage(), e);
    }
  }
}
