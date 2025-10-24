package com.sumukh.aaa.dto;

public record TailLookupResponse(
  String airline,
  String country,
  String model,
  String brand,
  Integer pax,
  Integer rangeKm,
  String seatLayout
) {}