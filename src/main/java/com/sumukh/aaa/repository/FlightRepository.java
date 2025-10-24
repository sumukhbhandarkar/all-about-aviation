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

  // Distinct airlines that touch an airport (origin or destination)
  @Query("select distinct f.airline from Flight f where f.origin = :ap or f.destination = :ap and f.airline is not null")
  List<Airline> findAirlinesServing(@Param("ap") Airport ap);

  // Distinct destinations reachable from the airport (either direction)
  @Query("""
         select distinct case when f.origin = :ap then f.destination else f.origin end
         from Flight f
         where f.origin = :ap or f.destination = :ap
         """)
  List<Airport> findConnectedAirports(@Param("ap") Airport ap);
}

