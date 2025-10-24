package com.sumukh.aaa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.*;

@Entity @Table(name = "airlines", indexes = {
  @Index(name = "idx_airline_name", columnList = "name", unique = true)
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Airline {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String name;
  @NotBlank private String baseCountry;
  private String logoUrl;

  // Codeshares stored as a simple collection table
  @ElementCollection
  @CollectionTable(name = "airline_codeshares", joinColumns = @JoinColumn(name = "airline_id"))
  @Column(name = "codeshare")
  private List<String> codeshares = new ArrayList<>();
}
