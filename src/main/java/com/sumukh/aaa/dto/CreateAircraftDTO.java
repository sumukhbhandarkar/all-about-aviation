package com.sumukh.aaa.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateAircraftDTO(
  @NotBlank String brand, @NotBlank String model,
  String seatLayoutJson, @Min(1) int paxNumber, @Min(1) int rangeKm
) {}