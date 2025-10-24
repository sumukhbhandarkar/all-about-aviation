package com.sumukh.aaa.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateAirlineDTO(
  @NotBlank String name, @NotBlank String baseCountry, String logoUrl, List<String> codeshares
) {}