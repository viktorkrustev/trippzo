package com.trippzo.repository;

import com.trippzo.model.Trip;
import com.trippzo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByDriver(User user);

    List<Trip> findByPassengersUser(User user);

    int deleteByDepartureDateTimeBefore(LocalDateTime dateTime);

    Page<Trip> findAll(Specification<Trip> spec, Pageable pageable);
}
