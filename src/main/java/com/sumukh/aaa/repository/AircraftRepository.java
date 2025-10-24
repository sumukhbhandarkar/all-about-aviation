package com.sumukh.aaa.repository;

import com.sumukh.aaa.model.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
  List<Aircraft> findByBrandIgnoreCaseAndModelIgnoreCase(String brand, String model);
  List<Aircraft> findByBrandIgnoreCase(String brand);
}