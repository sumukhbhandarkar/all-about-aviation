package com.sumukh.aaa.dto;

import java.util.List;

public record AirportLookupResponse(
        String iata,
        String city,
        List<String> runways,           // e.g., ["09L/27R","10/28"]
        List<String> airlines,          // airline names
        List<String> destinations,       // IATA codes connected by any flight
        String currentTime
) {}