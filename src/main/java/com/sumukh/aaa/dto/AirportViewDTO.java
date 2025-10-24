package com.sumukh.aaa.dto;

public record AirportViewDTO(
  String iataCode, String city, String address,
  double latitude, double longitude, String timeZoneId,
  String currentTime // computed per timeZoneId
) {}