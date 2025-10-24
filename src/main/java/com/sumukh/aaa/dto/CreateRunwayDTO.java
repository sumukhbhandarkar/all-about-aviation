package com.sumukh.aaa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateRunwayDTO(
  @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String airportIata,
  @NotBlank @Pattern(regexp = "^[0-3][0-9][LCR]?(/[0-3][0-9][LCR]?)?$") String identifier,
  @PositiveOrZero Integer lengthMeters,
  @PositiveOrZero Integer widthMeters,
  String surface
) {}