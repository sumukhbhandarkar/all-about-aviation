package com.sumukh.aaa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "flights", indexes = {
  @Index(name = "idx_flight_number", columnList = "flightNumber", unique = true),
  @Index(name = "idx_flight_origin_dest", columnList = "origin_id,destination_id"),
  @Index(name = "idx_flight_sched_dep", columnList = "scheduledDeparture")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Flight {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String flightNumber; // e.g., AI-502

  @NotNull private OffsetDateTime scheduledDeparture;
  @NotNull private OffsetDateTime scheduledArrival;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "origin_id")
  @NotNull private Airport origin;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "destination_id")
  @NotNull private Airport destination;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "airline_id")
  private Airline airline;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tail_number_id")
  private TailNumber tailNumber;
}
