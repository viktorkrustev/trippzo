package com.trippzo.repository;

import com.trippzo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRevieweeId(Long revieweeId);

    List<Review> findByTripId(Long tripId);

    Optional<Review> findByTripIdAndReviewerId(Long tripId, Long reviewerId);

    int countByRevieweeId(Long driverId);

    boolean existsByTripIdAndReviewerId(Long tripId, Long reviewerId);
}
