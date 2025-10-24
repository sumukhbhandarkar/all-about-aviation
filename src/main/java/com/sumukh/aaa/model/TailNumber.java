package com.sumukh.aaa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity @Table(name = "tail_numbers", indexes = {
  @Index(name = "idx_tail_unique", columnList = "tailNumber", unique = true),
  @Index(name = "idx_tail_country", columnList = "country")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TailNumber {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String tailNumber;  // e.g., VT-XYZ, N123AB
  @NotBlank private String country;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "airline_id")
  private Airline airline;              // nullable allowed

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "aircraft_id")
  private Aircraft aircraftType;        // links to Aircraft
}
