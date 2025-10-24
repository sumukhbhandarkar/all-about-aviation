package com.sumukh.aaa.dto;

public record FlightLookupResponse(
  String airline,           // Airline name (nullable if unknown)
  String from,              // origin IATA
  String to,                // destination IATA
  String scheduledDeparture,
  String scheduledArrival,
  String tailNumber,
  String model,
  String brand,
  String seatLayout,
  Integer numOfPaxTotal,
  Integer rangeKm
) {}