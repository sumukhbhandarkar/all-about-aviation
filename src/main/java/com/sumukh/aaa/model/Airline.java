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
  @Column(name = "codeshare", length = 3, nullable = false)
  @Builder.Default
  private List<String> codeshares = new ArrayList<>();

  // Extra safety if something ever tries to set null
  public void setCodeshares(List<String> cs) {
    this.codeshares = (cs == null) ? new ArrayList<>() : new ArrayList<>(cs);
  }

  public List<String> getCodeshares() {
    return (codeshares == null) ? List.of() : codeshares;
  }
}
