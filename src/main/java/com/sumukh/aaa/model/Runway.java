package com.sumukh.aaa.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "runways")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Runway {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "airport_id", nullable = false)
  private Airport airport;

  // e.g., "09L/27R" or simple "09L"
  private String identifier;

  // optional details
  private Integer lengthMeters;
  private Integer widthMeters;
  private String surface;   // e.g., ASPHALT, CONCRETE
}
