package com.sumukh.aaa.service;

import com.sumukh.aaa.dto.*;
import com.sumukh.aaa.model.*;
import com.sumukh.aaa.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class AviationService {
  private final AirportRepository airportRepo;
  private final AircraftRepository aircraftRepo;
  private final AirlineRepository airlineRepo;
  private final TailNumberRepository tailRepo;
  private final FlightRepository flightRepo;

  // Airports
  public Airport createAirport(CreateAirportDTO dto) {
    airportRepo.findByIataCode(dto.iataCode()).ifPresent(a -> {
      throw new IllegalArgumentException("Airport IATA already exists: " + dto.iataCode());
    });
    return airportRepo.save(Airport.builder()
      .iataCode(dto.iataCode().toUpperCase())
      .city(dto.city())
      .address(dto.address())
      .latitude(dto.latitude())
      .longitude(dto.longitude())
      .timeZoneId(dto.timeZoneId())
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
  public Airline createAirline(CreateAirlineDTO dto) {
    airlineRepo.findByNameIgnoreCase(dto.name()).ifPresent(a -> {
      throw new IllegalArgumentException("Airline exists: " + dto.name());
    });
    Airline a = Airline.builder()
      .name(dto.name())
      .baseCountry(dto.baseCountry())
      .logoUrl(dto.logoUrl())
      .build();
    if (dto.codeshares() != null) a.getCodeshares().addAll(dto.codeshares());
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
}
