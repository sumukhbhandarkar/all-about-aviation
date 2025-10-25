package com.sumukh.aaa.dto;

import java.util.List;

public record AirportLookupResponse(
        String iata,
        String city,
        List<String> runways,
        List<String> airlines,
        List<String> destinations,
        String localTime,
        List<NearbyAirportDTO> nearbyAirports
) {
    // compact ctor: no parameter list, no explicit field writes needed
    public AirportLookupResponse {
        runways        = runways        == null ? List.of() : List.copyOf(runways);
        airlines       = airlines       == null ? List.of() : List.copyOf(airlines);
        destinations   = destinations   == null ? List.of() : List.copyOf(destinations);
        nearbyAirports = nearbyAirports == null ? List.of() : List.copyOf(nearbyAirports);
        // iata/city/localTime can be left as-is or validated here
    }

    public static record NearbyAirport(String iata, String city, double distanceKm) {}
}
