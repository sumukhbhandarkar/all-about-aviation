package com.sumukh.aaa.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateAirlineDTO(
        @NotBlank String name,
        @NotBlank String baseCountry,
        String logoUrl,
        @Size(max = 50)
        List<@Pattern(regexp = "^[A-Za-z0-9]{2,3}$") String> codeshares // may be null
) {}