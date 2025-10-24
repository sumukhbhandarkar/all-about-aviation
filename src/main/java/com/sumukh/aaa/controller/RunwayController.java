// src/main/java/.../api/RunwayController.java
package com.sumukh.aaa.controller;

import com.sumukh.aaa.dto.*;
import com.sumukh.aaa.model.*;
import com.sumukh.aaa.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RunwayController {
  private final AirportRepository airportRepo;
  private final RunwayRepository runwayRepo;

  @PostMapping("/runways")
  public Runway addRunway(@RequestBody @Valid CreateRunwayDTO dto) {
    Airport ap = airportRepo.findByIataCode(dto.airportIata().toUpperCase())
        .orElseThrow(() -> new EntityNotFoundException("Airport not found: " + dto.airportIata()));
    return runwayRepo.save(Runway.builder()
        .airport(ap)
        .identifier(dto.identifier())
        .lengthMeters(dto.lengthMeters())
        .widthMeters(dto.widthMeters())
        .surface(dto.surface())
        .build());
  }

  @GetMapping("/airports/{iata}/runways")
  public List<Runway> listRunways(@PathVariable String iata) {
    Airport ap = airportRepo.findByIataCode(iata.toUpperCase())
        .orElseThrow(() -> new EntityNotFoundException("Airport not found: " + iata));
    return runwayRepo.findByAirport(ap);
  }
}
