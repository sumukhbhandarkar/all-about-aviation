package com.sumukh.aaa.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;

public record TailLookupResponse(
  String airline,
  String country,
  String model,
  String brand,
  Integer pax,
  Integer rangeKm,
  String seatLayout
) {}