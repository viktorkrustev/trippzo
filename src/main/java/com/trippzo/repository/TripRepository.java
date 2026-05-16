package com.trippzo.repository;

import com.trippzo.model.Trip;
import com.trippzo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    Page<Trip> findAll(Specification<Trip> spec, Pageable pageable);

    @EntityGraph(attributePaths = { "driver", "passengers" })
    Optional<Trip> findById(Long id);

    List<Trip> findByDriver(User user);

    List<Trip> findByPassengersUser(User user);

    int countByDriver(User user);

    int countByPassengersUser(User user);

    @Modifying
    @Transactional
    int deleteByDepartureDateTimeBefore(LocalDateTime dateTime);
}
