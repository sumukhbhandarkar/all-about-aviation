package com.sumukh.aaa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity @Table(name = "airports", indexes = {
        @Index(name = "idx_airport_iata", columnList = "iataCode", unique = true),
        @Index(name = "idx_airport_city", columnList = "city")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 3, max = 3)
    private String iataCode;

    @NotBlank
    private String city;

    @NotBlank
    private String address;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotBlank
    private String timeZoneId;
}
