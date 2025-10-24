package com.sumukh.aaa.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateFlightDTO(
  @NotBlank String flightNumber,
  @NotNull OffsetDateTime scheduledDeparture,
  @NotNull OffsetDateTime scheduledArrival,
  @NotBlank String originIata,
  @NotBlank String destinationIata,
  String airlineName,
  String tailNumber
) {}