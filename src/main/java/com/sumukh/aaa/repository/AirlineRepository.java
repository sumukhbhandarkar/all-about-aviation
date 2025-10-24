package com.sumukh.aaa.repository;

import com.sumukh.aaa.model.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, Long> {

  Optional<Airline> findByNameIgnoreCase(String name);

  @Query("select a from Airline a join a.codeshares c where lower(c) = lower(:code)")
  List<Airline> findByCodeshare(String code);
}