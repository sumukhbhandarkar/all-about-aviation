package com.sumukh.aaa.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateAirportDTO(
  @NotBlank @Size(min=3,max=3) String iataCode,
  @NotBlank String city,
  @NotBlank String address,
  @NotNull Double latitude,
  @NotNull Double longitude,
  @NotBlank String timeZoneId
) {}