package com.sumukh.aaa.repository;

import com.sumukh.aaa.model.Airline;
import com.sumukh.aaa.model.Airport;
import com.sumukh.aaa.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

  Optional<Flight> findByFlightNumber(String flightNumber);

  // ✅ Custom query to find flights by origin & destination IATA codes
  @Query("""
        SELECT f FROM Flight f
        WHERE UPPER(f.origin.iataCode) = UPPER(:originIata)
          AND UPPER(f.destination.iataCode) = UPPER(:destinationIata)
    """)
  List<Flight> findByRoute(@Param("originIata") String originIata,
                           @Param("destinationIata") String destinationIata);

  // For time range searches
  List<Flight> findByScheduledDepartureBetween(OffsetDateTime from, OffsetDateTime to);

  // Distinct airlines that service an airport
  @Query("""
        SELECT DISTINCT f.airline FROM Flight f
        WHERE (f.origin = :airport OR f.destination = :airport)
          AND f.airline IS NOT NULL
    """)
  List<Airline> findAirlinesServing(@Param("airport") Airport airport);

  // Distinct destinations reachable from/to this airport
  @Query("""
        SELECT DISTINCT CASE WHEN f.origin = :airport THEN f.destination ELSE f.origin END
        FROM Flight f
        WHERE f.origin = :airport OR f.destination = :airport
    """)
  List<Airport> findConnectedAirports(@Param("airport") Airport airport);
}

