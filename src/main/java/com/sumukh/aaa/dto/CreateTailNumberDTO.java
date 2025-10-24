package com.sumukh.aaa.dto;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateTailNumberDTO(
  @NotBlank String tailNumber, @NotBlank String country,
  String airlineName,        // resolve by name (optional)
  String aircraftBrand,      // resolve pair if present
  String aircraftModel
) {}