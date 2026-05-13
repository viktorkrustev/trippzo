package com.trippzo.service;

import com.trippzo.model.Review;
import com.trippzo.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional
    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsForDriver(Long driverId) {
        return reviewRepository.findByRevieweeId(driverId);
    }

    public List<Review> getReviewsForTrip(Long tripId) {
        return reviewRepository.findByTripId(tripId);
    }

    public double getAverageRatingForDriver(Long driverId) {
        List<Review> reviews = getReviewsForDriver(driverId);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        return BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    public int getReviewCountForDriver(Long driverId) {
        return reviewRepository.countByRevieweeId(driverId);
    }

    public Optional<Review> getReviewByTripAndReviewer(Long tripId, Long reviewerId) {
        return reviewRepository.findByTripIdAndReviewerId(tripId, reviewerId);
    }

    public Optional<Review> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    public boolean hasUserReviewedTrip(Long tripId, Long reviewerId) {
        return reviewRepository.existsByTripIdAndReviewerId(tripId, reviewerId);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
