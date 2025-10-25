package com.sumukh.aaa.controller;

import com.sumukh.aaa.dto.AirportLookupResponse;
import com.sumukh.aaa.dto.DestAirport;
import com.sumukh.aaa.dto.NearbyAirportDTO;
import com.sumukh.aaa.model.*;
import com.sumukh.aaa.repository.*;
import com.sumukh.aaa.service.AviationService;
import com.sumukh.aaa.utils.MapUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  private MapUtils mapUtils;


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

    // NEW: make sure the template has objects when it expects d.iata / d.name
    var destFull = vm.destinationsFull() != null ? vm.destinationsFull() : java.util.List.<DestAirport>of();
    model.addAttribute("destinationsFull", destFull);

    if (vm.ap().getLatitude() != null && vm.ap().getLongitude() != null) {
      double lat = vm.ap().getLatitude();
      double lon = vm.ap().getLongitude();
      var lines = mapUtils.buildRunwayLines(lat, lon, vm.runways());

      var mapModel = new MapUtils.MapModel(
              lat, lon,
              vm.ap().getIataCode(),
              vm.ap().getCity(),
              lines
      );
      model.addAttribute("map", mapModel);
    }

    return "airport";
  }


  // ---------- Shared loader for both endpoints ----------
  private ViewModel loadAirportViewModel(String code) {
    Airport ap = airportRepo.findByIataCodeIgnoreCase(code)
            .orElseThrow(() -> new EntityNotFoundException("Airport not found: " + code));

    var runways = runwayRepo.findByAirport(ap);

    // Keep full Airline objects, sorted by name for display
    var airlines = flightRepo.findAirlinesServing(ap).stream()
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(Airline::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();

    // IATA codes of connected airports (distinct + sorted)
    var destinations = flightRepo.findConnectedIataCodes(ap).stream()
            .filter(Objects::nonNull)
            .distinct()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();

    // Build full destination DTOs for the UI
    var destinationsFull = destinations.stream()
            .map(iata -> airportRepo.findByIataCodeIgnoreCase(iata)
                    .map(a -> new DestAirport(a.getIataCode(), a.getCity()))
                    .orElse(new DestAirport(iata, "Unknown Airport")))
            .toList();

    var localTime = formatLocalTime(ap.getTimeZoneId());

    var nearby = (ap.getLatitude() == null || ap.getLongitude() == null)
            ? List.<AirportDistanceProjection>of()
            : svc.nearestAirports(ap, 5);

    var runwayIdents = runways.stream()
            .map(Runway::getIdentifier)
            .filter(Objects::nonNull)
            .toList();

    // Use the canonical constructor: ap, runways, airlines, runwayIdents, destinations, localTime, nearby, destinationsFull
    return new ViewModel(ap, runways, airlines, runwayIdents, destinations, localTime, nearby, destinationsFull);
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
          List<AirportDistanceProjection> nearby,
          List<DestAirport> destinationsFull   // ← add this
  ) {
    // Convenience ctor: compute runwayIdents, default destinationsFull = empty list
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
              runways.stream().map(Runway::getIdentifier).toList(),
              destinations,
              localTime,
              nearby,
              java.util.List.of() // default when you don’t have full destinations yet
      );
    }
    // Helper derived view
    List<String> airlineNames() {
      return airlines.stream().map(Airline::getName).toList();
    }
  }

}
