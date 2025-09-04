package com.trippzo.repository;

import com.trippzo.model.Trip;
import com.trippzo.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByOriginContainingIgnoreCaseAndDestinationContainingIgnoreCase(String origin, String destination);
    List<Trip> findByDepartureDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Trip> findByOriginContainingIgnoreCaseAndDestinationContainingIgnoreCaseAndDepartureDateTimeBetween(
            String origin, String destination, LocalDateTime start, LocalDateTime end
    );

    List<Trip> findByDriver(User driver);

    // Намира пътувания, където user е пътник
    List<Trip> findByPassengersUser(User user);

    List<Trip> findByOriginContainingIgnoreCase(String origin);
    List<Trip> findByDestinationContainingIgnoreCase(String destination);
    List<Trip> findByOriginContainingIgnoreCaseAndDepartureDateTimeBetween(String origin, LocalDateTime start, LocalDateTime end);
    List<Trip> findByDestinationContainingIgnoreCaseAndDepartureDateTimeBetween(String destination, LocalDateTime start, LocalDateTime end);

    @Transactional
    void deleteByDepartureDateTimeBefore(LocalDateTime dateTime);


}
