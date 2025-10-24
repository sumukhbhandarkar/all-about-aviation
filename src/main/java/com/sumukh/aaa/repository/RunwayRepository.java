package com.sumukh.aaa.repository;

import com.sumukh.aaa.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface RunwayRepository extends JpaRepository<Runway, Long> {
  List<Runway> findByAirport(Airport airport);
}
