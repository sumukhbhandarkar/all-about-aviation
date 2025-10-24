package com.sumukh.aaa.repository;

import com.sumukh.aaa.model.TailNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TailNumberRepository extends JpaRepository<TailNumber, Long> {
  Optional<TailNumber> findByTailNumber(String tailNumber);
  List<TailNumber> findByCountryIgnoreCase(String country);
}