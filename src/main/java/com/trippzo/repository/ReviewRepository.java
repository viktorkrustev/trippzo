package com.trippzo.repository;

import com.trippzo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRevieweeId(Long revieweeId);

    List<Review> findByTripId(Long tripId);

    int countByRevieweeId(Long driverId);

    boolean existsByTripIdAndReviewerId(Long tripId, Long reviewerId);
}
