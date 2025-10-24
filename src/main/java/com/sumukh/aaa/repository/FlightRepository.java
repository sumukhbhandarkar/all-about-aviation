package com.sumukh.aaa.repository;

import com.sumukh.aaa.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
  Optional<Flight> findByFlightNumber(String flightNumber);
  @Query("select f from Flight f where f.origin.iataCode = :origin and f.destination.iataCode = :dest")
  List<Flight> findByRoute(String origin, String dest);
  List<Flight> findByScheduledDepartureBetween(OffsetDateTime from, OffsetDateTime to);
}