package com.sumukh.aaa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateRunwayDTO(
  @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String airportIata,
  @Pattern(
          regexp = "^[0-9]{1,2}[LCR]?(/[0-9]{1,2}[LCR]?)?$",
          message = "Identifier must look like 09/27 or 09L/27R"
  )
  String identifier,
  @PositiveOrZero Integer lengthMeters,
  @PositiveOrZero Integer widthMeters,
  String surface
) {}