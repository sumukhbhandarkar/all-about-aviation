package com.sumukh.aaa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity @Table(name = "aircrafts", indexes = {
  @Index(name = "idx_aircraft_brand_model", columnList = "brand,model")
})
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class Aircraft {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String brand;       // e.g., Airbus, Boeing
  @NotBlank private String model;       // e.g., A320neo, 737-8

  @Column(name = "seat_layout_json", columnDefinition = "text")
  private String seatLayoutJson;        // store JSON as TEXT

  @Min(1) private int paxNumber;        // total seats
  @Min(1) private int rangeKm;          // range in km
}
