package com.sumukh.aaa.controller;

import com.sumukh.aaa.dto.*;
import com.sumukh.aaa.model.*;
import com.sumukh.aaa.repository.*;
import com.sumukh.aaa.service.AviationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AviationController {
  private final AviationService svc;
  private final AirportRepository airportRepo;
  private final AircraftRepository aircraftRepo;
  private final AirlineRepository airlineRepo;
  private final TailNumberRepository tailRepo;
  private final FlightRepository flightRepo;

  // ---------- Airports ----------
//  @PostMapping("/airports")
//  public Airport createAirport(@RequestBody @Valid CreateAirportDTO dto) {
//    return svc.createAirport(dto);
//  }

  @PostMapping("/airports")
  public Airport upsertAirport(
          @Valid @RequestBody CreateAirportDTO dto,
          @RequestParam(name = "upsert", defaultValue = "true") boolean upsert
  ) {
    return svc.createOrUpdateAirport(dto, upsert);
  }

  @GetMapping("/airports")
  public List<AirportViewDTO> searchAirports(
      @RequestParam(required = false) String iata,
      @RequestParam(required = false) String city) {
    List<Airport> list =
      (iata != null) ? airportRepo.findByIataCode(iata.toUpperCase()).map(List::of).orElse(List.of())
      : (city != null) ? airportRepo.findByCityContainingIgnoreCase(city)
      : airportRepo.findAll();

    return list.stream().map(this::toAirportView).collect(Collectors.toList());
  }

  private AirportViewDTO toAirportView(Airport a) {
    // compute currentTime per timezone
    String current = ZonedDateTime.now(ZoneId.of(a.getTimeZoneId())).toString();
    return new AirportViewDTO(
      a.getIataCode(), a.getCity(), a.getAddress(),
      a.getLatitude(), a.getLongitude(), a.getTimeZoneId(),
      current
    );
  }

  // ---------- Aircrafts ----------
  @PostMapping("/aircrafts")
  public Aircraft createAircraft(@RequestBody @Valid CreateAircraftDTO dto) {
    return svc.createAircraft(dto);
  }

  @GetMapping("/aircrafts")
  public List<Aircraft> searchAircrafts(
      @RequestParam(required = false) String brand,
      @RequestParam(required = false) String model) {
    if (brand != null && model != null) return aircraftRepo.findByBrandIgnoreCaseAndModelIgnoreCase(brand, model);
    if (brand != null) return aircraftRepo.findByBrandIgnoreCase(brand);
    return aircraftRepo.findAll();
  }

  // ---------- Airlines ----------
  @PostMapping("/airlines")
  public Airline createAirline(@RequestBody @Valid CreateAirlineDTO dto) {
    return svc.createOrUpdateAirline(dto, true);
  }

  @GetMapping("/airlines")
  public List<Airline> searchAirlines(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String codeshare) {
    if (name != null) return airlineRepo.findByNameIgnoreCase(name).map(List::of).orElse(List.of());
    if (codeshare != null) return airlineRepo.findByCodeshare(codeshare);
    return airlineRepo.findAll();
  }

  // ---------- Tail Numbers ----------
  @PostMapping("/tails")
  public TailNumber createTail(@RequestBody @Valid CreateTailNumberDTO dto) {
    return svc.createTailNumber(dto);
  }

  @GetMapping("/tails")
  public List<TailNumber> searchTails(
      @RequestParam(required = false) String tailNumber,
      @RequestParam(required = false) String country) {
    if (tailNumber != null) return tailRepo.findByTailNumber(tailNumber.toUpperCase()).map(List::of).orElse(List.of());
    if (country != null) return tailRepo.findByCountryIgnoreCase(country);
    return tailRepo.findAll();
  }

  // ---------- Flights ----------
  @PostMapping("/flights")
  public Flight createFlight(@RequestBody @Valid CreateFlightDTO dto) {
    return svc.createFlight(dto);
  }

  @GetMapping("/flights")
  public List<Flight> searchFlights(
      @RequestParam(required = false) String flightNumber,
      @RequestParam(required = false) String originIata,
      @RequestParam(required = false) String destinationIata,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
  ) {
    if (flightNumber != null) return flightRepo.findByFlightNumber(flightNumber.toUpperCase()).map(List::of).orElse(List.of());
    if (originIata != null && destinationIata != null) return flightRepo.findByRoute(originIata.toUpperCase(), destinationIata.toUpperCase());
    if (from != null && to != null) return flightRepo.findByScheduledDepartureBetween(from, to);
    return flightRepo.findAll();
  }
}
