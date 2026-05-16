package com.trippzo.repository;

import com.trippzo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRevieweeId(Long revieweeId);

    List<Review> findByTripId(Long tripId);

    int countByRevieweeId(Long driverId);

    boolean existsByTripIdAndReviewerId(Long tripId, Long reviewerId);

    @Query("""
            SELECT r.reviewee.id, AVG(r.rating)
            FROM Review r
            WHERE r.reviewee.id IN :driverIds
            GROUP BY r.reviewee.id
            """)
    List<Object[]> findAverageRatingsByDriverIds(@Param("driverIds") List<Long> driverIds);
}
