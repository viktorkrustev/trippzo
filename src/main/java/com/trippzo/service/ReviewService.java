package com.trippzo.service;

import com.trippzo.model.Review;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public void createAndSaveReview(Trip trip, User reviewer, User reviewee, int rating, String comment) {
        if (!isValidRating(rating)) {
            throw new IllegalArgumentException("Оценката трябва да е между 1 и 5.");
        }

        if (hasUserReviewedTrip(trip.getId(), reviewer.getId())) {
            throw new IllegalStateException("Вече сте оценили това пътуване.");
        }

        Review review = new Review();
        review.setTrip(trip);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    public List<Review> getReviewsForDriver(Long driverId) {
        return reviewRepository.findByRevieweeId(driverId);
    }

    public List<Review> getReviewsForTrip(Long tripId) {
        return reviewRepository.findByTripId(tripId);
    }

    @Transactional(readOnly = true)
    public Map<Long, Double> getDriverRatingsForTrips(List<Trip> trips) {
        Map<Long, Double> driverRatings = new HashMap<>();
        for (Trip trip : trips) {
            Long driverId = trip.getDriver().getId();
            if (!driverRatings.containsKey(driverId)) {
                driverRatings.put(driverId, getAverageRatingForDriver(driverId));
            }
        }
        return driverRatings;
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
