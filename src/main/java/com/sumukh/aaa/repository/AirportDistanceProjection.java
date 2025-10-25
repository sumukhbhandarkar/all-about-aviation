package com.sumukh.aaa.repository;

public interface AirportDistanceProjection {
  String getIataCode();
  String getCity();
  Double getLatitude();
  Double getLongitude();
  Double getDistanceKm();
}