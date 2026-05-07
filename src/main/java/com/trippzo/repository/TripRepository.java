package com.trippzo.repository;

import com.trippzo.model.Trip;
import com.trippzo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    Page<Trip> findByOriginContainingIgnoreCaseAndDestinationContainingIgnoreCaseAndDepartureDateTimeBetween(
            String origin, String destination, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Trip> findByOriginContainingIgnoreCaseAndDestinationContainingIgnoreCase(String origin, String destination,
            Pageable pageable);

    Page<Trip> findByOriginContainingIgnoreCaseAndDepartureDateTimeBetween(String origin, LocalDateTime start,
            LocalDateTime end, Pageable pageable);

    Page<Trip> findByDestinationContainingIgnoreCaseAndDepartureDateTimeBetween(String destination, LocalDateTime start,
            LocalDateTime end, Pageable pageable);

    Page<Trip> findByOriginContainingIgnoreCase(String origin, Pageable pageable);

    Page<Trip> findByDestinationContainingIgnoreCase(String destination, Pageable pageable);

    Page<Trip> findByDepartureDateTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Trip> findAll(Pageable pageable);

    List<Trip> findByDriver(User user);

    List<Trip> findByPassengersUser(User user);
}
