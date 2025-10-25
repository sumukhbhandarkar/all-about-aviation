package com.sumukh.aaa.repository;

import com.sumukh.aaa.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
  Optional<Airport> findByIataCode(String iataCode);
  Optional<Airport> findByIataCodeIgnoreCase(String iataCode);
  List<Airport> findByCityContainingIgnoreCase(String city);

  @Query(value = """
SELECT 
  a.iata_code  AS iataCode,
  a.city       AS city,
  a.latitude   AS latitude,
  a.longitude  AS longitude,
  (6371 * acos(
      cos(radians(:lat)) * cos(radians(a.latitude)) *
      cos(radians(a.longitude) - radians(:lon)) +
      sin(radians(:lat)) * sin(radians(a.latitude))
  )) AS distanceKm
FROM airports a
WHERE a.latitude IS NOT NULL
  AND a.longitude IS NOT NULL
  AND upper(a.iata_code) <> upper(:excludeIata)
ORDER BY distanceKm ASC
LIMIT 5
""", nativeQuery = true)
  List<AirportDistanceProjection> findNearestAirports(@Param("lat") double lat,
                      @Param("lon") double lon,
                      @Param("excludeIata") String excludeIata);
}