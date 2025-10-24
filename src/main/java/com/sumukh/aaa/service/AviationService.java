package com.sumukh.aaa.service;

import com.sumukh.aaa.dto.*;
import com.sumukh.aaa.model.*;
import com.sumukh.aaa.repository.*;
import com.sumukh.aaa.utils.TimeZoneResolver;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.sumukh.aaa.utils.Normalizers.iata;

@Service @RequiredArgsConstructor @Transactional
public class AviationService {
  private final AirportRepository airportRepo;
  private final AircraftRepository aircraftRepo;
  private final AirlineRepository airlineRepo;
  private final TailNumberRepository tailRepo;
  private final FlightRepository flightRepo;

  // Airports
  public Airport createAirport(CreateAirportDTO dto) {
    String iataCode = iata(dto.iataCode());
    // Required + range validation
    if (dto.latitude() == null || dto.longitude() == null)
      throw new IllegalArgumentException("Latitude/Longitude must be provided");
    if (dto.latitude() < -90 || dto.latitude() > 90)
      throw new IllegalArgumentException("Latitude out of range (-90..90)");
    if (dto.longitude() < -180 || dto.longitude() > 180)
      throw new IllegalArgumentException("Longitude out of range (-180..180)");

    airportRepo.findByIataCode(iataCode).ifPresent(a -> {
      throw new IllegalArgumentException("Airport IATA already exists: " + iataCode);
    });

    // Resolve time zone (IANA or offset, or auto from lat/lon if blank)
    String tzId = TimeZoneResolver.resolve(dto.timeZoneId(), dto.latitude(), dto.longitude());

    return airportRepo.save(Airport.builder()
            .iataCode(iataCode)
            .city(dto.city().trim())
            .address(dto.address().trim())
            .latitude(dto.latitude())
            .longitude(dto.longitude())
            .timeZoneId(tzId)
            .build());
  }

  // Aircrafts
  public Aircraft createAircraft(CreateAircraftDTO dto) {
    return aircraftRepo.save(Aircraft.builder()
      .brand(dto.brand()).model(dto.model())
      .seatLayoutJson(dto.seatLayoutJson())
      .paxNumber(dto.paxNumber()).rangeKm(dto.rangeKm())
      .build());
  }

  // Airlines
//  public Airline createAirline(CreateAirlineDTO dto) {
//    airlineRepo.findByNameIgnoreCase(dto.name()).ifPresent(a -> {
//      throw new IllegalArgumentException("Airline exists: " + dto.name());
//    });
//    Airline a = Airline.builder()
//      .name(dto.name())
//      .baseCountry(dto.baseCountry())
//      .logoUrl(dto.logoUrl())
//      .build();
//    if (dto.codeshares() != null) a.getCodeshares().addAll(dto.codeshares());
//    return airlineRepo.save(a);
//  }
  private static List<String> normCodeshares(List<String> in) {
    if (in == null) return new ArrayList<>();
    return in.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toUpperCase)
            .distinct()
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
  }

  @Transactional
  public Airline createOrUpdateAirline(CreateAirlineDTO dto, boolean upsert) {
    String name = dto.name().trim();
    List<String> cs = normCodeshares(dto.codeshares());

    var existing = airlineRepo.findByNameIgnoreCase(name);

    if (existing.isPresent()) {
      if (!upsert) throw new IllegalArgumentException("Airline exists: " + name);
      Airline a = existing.get();
      a.setBaseCountry(dto.baseCountry().trim());
      a.setLogoUrl(dto.logoUrl());
      a.setCodeshares(cs);
      return airlineRepo.save(a);
    }
    Airline a = Airline.builder()
            .name(name)
            .baseCountry(dto.baseCountry().trim())
            .logoUrl(dto.logoUrl())
            .codeshares(cs)
            .build();

    return airlineRepo.save(a);
  }


  // TailNumber
  public TailNumber createTailNumber(CreateTailNumberDTO dto) {
    tailRepo.findByTailNumber(dto.tailNumber()).ifPresent(t -> {
      throw new IllegalArgumentException("Tail already exists: " + dto.tailNumber());
    });
    Airline airline = (dto.airlineName() == null) ? null :
      airlineRepo.findByNameIgnoreCase(dto.airlineName())
        .orElseThrow(() -> new EntityNotFoundException("Airline not found: " + dto.airlineName()));

    Aircraft aircraft = null;
    if (dto.aircraftBrand() != null && dto.aircraftModel() != null) {
      aircraft = aircraftRepo.findByBrandIgnoreCaseAndModelIgnoreCase(dto.aircraftBrand(), dto.aircraftModel())
        .stream().findFirst()
        .orElseThrow(() -> new EntityNotFoundException("Aircraft not found: " + dto.aircraftBrand() + " " + dto.aircraftModel()));
    }

    return tailRepo.save(TailNumber.builder()
      .tailNumber(dto.tailNumber().toUpperCase())
      .country(dto.country())
      .airline(airline)
      .aircraftType(aircraft)
      .build());
  }

  // Flights
  public Flight createFlight(CreateFlightDTO dto) {
    flightRepo.findByFlightNumber(dto.flightNumber()).ifPresent(f -> {
      throw new IllegalArgumentException("Flight exists: " + dto.flightNumber());
    });
    Airport origin = airportRepo.findByIataCode(dto.originIata().toUpperCase())
      .orElseThrow(() -> new EntityNotFoundException("Origin airport not found: " + dto.originIata()));
    Airport dest = airportRepo.findByIataCode(dto.destinationIata().toUpperCase())
      .orElseThrow(() -> new EntityNotFoundException("Destination airport not found: " + dto.destinationIata()));

    Airline airline = (dto.airlineName() == null) ? null :
      airlineRepo.findByNameIgnoreCase(dto.airlineName())
        .orElseThrow(() -> new EntityNotFoundException("Airline not found: " + dto.airlineName()));
    TailNumber tail = (dto.tailNumber() == null) ? null :
      tailRepo.findByTailNumber(dto.tailNumber().toUpperCase())
        .orElseThrow(() -> new EntityNotFoundException("Tail not found: " + dto.tailNumber()));

    return flightRepo.save(Flight.builder()
      .flightNumber(dto.flightNumber().toUpperCase())
      .scheduledDeparture(dto.scheduledDeparture())
      .scheduledArrival(dto.scheduledArrival())
      .origin(origin).destination(dest)
      .airline(airline).tailNumber(tail)
      .build());
  }

  @Transactional
  public Airport createOrUpdateAirport(CreateAirportDTO dto, boolean upsert) {
    String code = iata(dto.iataCode());

    // required + range checks
    if (dto.latitude() == null || dto.longitude() == null)
      throw new IllegalArgumentException("Latitude/Longitude must be provided");
    if (dto.latitude() < -90 || dto.latitude() > 90)
      throw new IllegalArgumentException("Latitude out of range (-90..90)");
    if (dto.longitude() < -180 || dto.longitude() > 180)
      throw new IllegalArgumentException("Longitude out of range (-180..180)");

    var existing = airportRepo.findByIataCode(code);

    if (existing.isPresent()) {
      if (!upsert) {
        throw new IllegalArgumentException("Airport IATA already exists: " + code);
      }
      // UPDATE path
      Airport a = existing.get();
      a.setCity(dto.city().trim());
      a.setAddress(dto.address().trim());
      a.setLatitude(dto.latitude());
      a.setLongitude(dto.longitude());
      String tzId = TimeZoneResolver.resolve(dto.timeZoneId(), dto.latitude(), dto.longitude());
      a.setTimeZoneId(tzId);
//      a.setTimeZoneId(TimeZoneResolver.resolve(dto.timeZoneId(), dto.latitude(), dto.longitude()));
      return airportRepo.save(a);
    }

    // CREATE path
    Airport a = Airport.builder()
            .iataCode(code)
            .city(dto.city().trim())
            .address(dto.address().trim())
            .latitude(dto.latitude())
            .longitude(dto.longitude())
            .timeZoneId(TimeZoneResolver.resolve(dto.timeZoneId(), dto.latitude(), dto.longitude()))
            .build();
    return airportRepo.save(a);
  }
}
