package com.sumukh.aaa.repository;

import com.sumukh.aaa.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
  Optional<Airport> findByIataCode(String iataCode);
  List<Airport> findByCityContainingIgnoreCase(String city);
}