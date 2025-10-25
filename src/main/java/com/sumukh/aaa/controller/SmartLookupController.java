package com.sumukh.aaa.controller;

import com.sumukh.aaa.dto.AirportLookupResponse;
import com.sumukh.aaa.dto.NearbyAirportDTO;
import com.sumukh.aaa.model.*;
import com.sumukh.aaa.repository.*;
import com.sumukh.aaa.service.AviationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class SmartLookupController {

  private final AirportRepository airportRepo;
  private final RunwayRepository runwayRepo;
  private final FlightRepository flightRepo;
  private final AviationService svc;

  // ---------- JSON (API) ----------
  @GetMapping(value = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<AirportLookupResponse> airportJson(@PathVariable String code) {
    var result = loadAirportViewModel(code);

    var nearbyDtos = mapNearby(result.nearby()); // <-- use nearby(), not nearbyAirports()

    var dto = new AirportLookupResponse(
            result.ap().getIataCode(),
            result.ap().getCity(),
            result.runwayIdents(),
            result.airlineNames(),
            result.destinations(),
            result.localTime(),
            nearbyDtos
    );

    return ResponseEntity.ok(dto);
  }

  private static List<NearbyAirportDTO> mapNearby(List<AirportDistanceProjection> src) {
    if (src == null) return List.of();
    return src.stream()
            .map(p -> new NearbyAirportDTO(
                    p.getIataCode(),
                    p.getCity(),
                    p.getDistanceKm() == null ? 0.0 : p.getDistanceKm()
            ))
            .toList();
  }



  // ---------- HTML (Browser) ----------
  @GetMapping(value = "/{code}", produces = MediaType.TEXT_HTML_VALUE)
  public String airportHtml(@PathVariable String code, Model model) {
    var vm = loadAirportViewModel(code);
    model.addAttribute("ap", vm.ap());
    model.addAttribute("runways", vm.runways());
    model.addAttribute("airlines", vm.airlines());
    model.addAttribute("destinations", vm.destinations());
    model.addAttribute("localTime", vm.localTime());
    model.addAttribute("nearby", vm.nearby());
    return "airport"; // renders src/main/resources/templates/airport.html
  }

  // ---------- Shared loader for both endpoints ----------
  private ViewModel loadAirportViewModel(String code) {
    Airport ap = airportRepo.findByIataCodeIgnoreCase(code)
        .orElseThrow(() -> new EntityNotFoundException("Airport not found: " + code));

    var runways = runwayRepo.findByAirport(ap);

    var airlines = flightRepo.findAirlinesServing(ap); // List<Airline>
    var airlineNames = airlines.stream()
        .filter(Objects::nonNull)
        .map(Airline::getName)
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .toList();

    // Use your existing connected IATA query
    var destinations = flightRepo.findConnectedIataCodes(ap).stream()
        .filter(Objects::nonNull)
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .toList();

    var localTime = formatLocalTime(ap.getTimeZoneId());

    var nearby = (ap.getLatitude() == null || ap.getLongitude() == null)
        ? List.<AirportDistanceProjection>of()
        : svc.nearestAirports(ap, 5);

    return new ViewModel(ap, runways, airlines, airlineNames, destinations, localTime, nearby);
  }

  private String formatLocalTime(String zoneId) {
    if (zoneId == null || zoneId.isBlank()) return "—";
    ZoneId zone = ZoneId.of(zoneId);
    ZonedDateTime now = ZonedDateTime.now(zone);
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
        "EEEE, dd MMM yyyy, hh:mm a z '('VV')'", Locale.ENGLISH);
    return now.format(fmt);
  }

  // Small internal carrier for both JSON/HTML
  private record ViewModel(
          Airport ap,
          List<Runway> runways,
          List<Airline> airlines,
          List<String> runwayIdents,
          List<String> destinations,
          String localTime,
          List<AirportDistanceProjection> nearby
  ) {
    // ✅ Convenience constructor (NOT canonical): no runwayIdents param
    ViewModel(
            Airport ap,
            List<Runway> runways,
            List<Airline> airlines,
            List<String> destinations,
            String localTime,
            List<AirportDistanceProjection> nearby
    ) {
      this(
              ap,
              runways,
              airlines,
              runways.stream().map(Runway::getIdentifier).toList(), // compute here
              destinations,
              localTime,
              nearby
      );
    }

    // Helper derived view
    List<String> airlineNames() {
      return airlines.stream().map(Airline::getName).toList();
    }
  }

}
