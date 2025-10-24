package com.sumukh.aaa.controller;

import com.sumukh.aaa.model.*;
import com.sumukh.aaa.dto.*;
import com.sumukh.aaa.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class SmartLookupController {

  private final FlightRepository flightRepo;
  private final AirportRepository airportRepo;
  private final AirlineRepository airlineRepo;
  private final TailNumberRepository tailRepo;
  private final RunwayRepository runwayRepo;

  // Patterns (case-insensitive)
  private static final Pattern IATA3 = Pattern.compile("^[A-Z]{3}$", Pattern.CASE_INSENSITIVE);

  // Tail: common Indian style "VT-XXX" or "VTXXX", but also allow AA-ABC, AAABC (2-3 letters + optional hyphen + 2-5 letters/digits)
  private static final Pattern TAIL = Pattern.compile("^[A-Z]{2,3}-?[A-Z0-9]{2,5}$", Pattern.CASE_INSENSITIVE);

  // Flight: 2 alnum airline code + optional hyphen + 1–4 digits (allow leading zeros)
  private static final Pattern FLIGHT = Pattern.compile("^[A-Z0-9]{2}-?\\d{1,4}$", Pattern.CASE_INSENSITIVE);

  private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  @GetMapping("{code}")
  public Object universalLookup(@PathVariable String code) {
    String raw = code.trim();

    // Try flight first (matches your /6E-123, /6e0123 etc)
    if (FLIGHT.matcher(raw).matches()) {
      return lookupFlight(raw);
    }

    // Then tail (matches /VT-ILO, /vtilo, etc)
    if (TAIL.matcher(raw).matches()) {
      return lookupTail(raw);
    }

    // Then 3-letter IATA (matches /blr or /BLR)
    if (IATA3.matcher(raw).matches()) {
      return lookupAirport(raw);
    }

    throw new IllegalArgumentException("Unsupported code format: " + raw);
  }

  // ---------- helpers ----------

  private FlightLookupResponse lookupFlight(String code) {
    String normalized = normalizeFlight(code);        // e.g., "6E-0123" -> "6E0123"
    String alt = normalizedWithHyphen(normalized);    // "6E0123" -> "6E-0123"

    Optional<Flight> opt = flightRepo.findByFlightNumber(normalized);
    if (opt.isEmpty()) opt = flightRepo.findByFlightNumber(alt);

    Flight f = opt.orElseThrow(() -> new EntityNotFoundException("Flight not found: " + code));

    Airline airline = f.getAirline();
    TailNumber tail = f.getTailNumber();
    Aircraft ac = (tail != null) ? tail.getAircraftType() : null;

    return new FlightLookupResponse(
      airline != null ? airline.getName() : null,
      f.getOrigin() != null ? f.getOrigin().getIataCode() : null,
      f.getDestination() != null ? f.getDestination().getIataCode() : null,
      f.getScheduledDeparture() != null ? ISO.format(f.getScheduledDeparture()) : null,
      f.getScheduledArrival() != null ? ISO.format(f.getScheduledArrival()) : null,
      tail != null ? tail.getTailNumber() : null,
      ac != null ? ac.getModel() : null,
      ac != null ? ac.getBrand() : null,
      ac != null ? ac.getSeatLayoutJson() : null,
      ac != null ? ac.getPaxNumber() : null,
      ac != null ? ac.getRangeKm() : null
    );
  }

  private TailLookupResponse lookupTail(String code) {
    String normalized = normalizeTail(code); // e.g., "vt-ilo" -> "VT-ILO" and also check "VTILO"
    String alt = normalized.replace("-", "");

    Optional<TailNumber> opt = tailRepo.findByTailNumber(normalized);
    if (opt.isEmpty()) opt = tailRepo.findByTailNumber(alt);

    TailNumber t = opt.orElseThrow(() -> new EntityNotFoundException("Tail not found: " + code));

    Airline airline = t.getAirline();
    Aircraft ac = t.getAircraftType();

    return new TailLookupResponse(
      airline != null ? airline.getName() : null,
      t.getCountry(),
      ac != null ? ac.getModel() : null,
      ac != null ? ac.getBrand() : null,
      ac != null ? ac.getPaxNumber() : null,
      ac != null ? ac.getRangeKm() : null,
      ac != null ? ac.getSeatLayoutJson() : null
    );
  }

  private AirportLookupResponse lookupAirport(String code) {
    Airport ap = airportRepo.findByIataCode(code.toUpperCase())
        .orElseThrow(() -> new EntityNotFoundException("Airport not found: " + code));

    List<String> runways = runwayRepo.findByAirport(ap).stream()
        .map(Runway::getIdentifier).toList();

    List<String> airlines = flightRepo.findAirlinesServing(ap).stream()
        .filter(Objects::nonNull)
        .map(Airline::getName)
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .toList();

    List<String> destinations = flightRepo.findConnectedAirports(ap).stream()
        .filter(Objects::nonNull)
        .map(Airport::getIataCode)
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .toList();

    return new AirportLookupResponse(ap.getIataCode(), ap.getCity(), runways, airlines, destinations);
  }

  private static String normalizeFlight(String s) {
    String u = s.toUpperCase(Locale.ROOT);
    return u.replace("-", "");
  }

  private static String normalizedWithHyphen(String normalizedNoHyphen) {
    // Insert a hyphen after first 2 chars to try alternate storage
    if (normalizedNoHyphen.length() >= 3) {
      return normalizedNoHyphen.substring(0, 2) + "-" + normalizedNoHyphen.substring(2);
    }
    return normalizedNoHyphen;
  }

  private static String normalizeTail(String s) {
    String u = s.toUpperCase(Locale.ROOT);
    // Prefer "AA-ABC" style as primary
    if (!u.contains("-") && u.length() >= 3) {
      return u.substring(0, 2) + "-" + u.substring(2);
    }
    return u;
  }
}
