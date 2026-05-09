package com.trippzo.repository;

import com.trippzo.model.TripPassenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripPassengerRepository extends JpaRepository<TripPassenger, Long> {

    Optional<TripPassenger> findByTripIdAndUserId(Long tripId, Long userId);

    int countByTripId(Long tripId);
}
