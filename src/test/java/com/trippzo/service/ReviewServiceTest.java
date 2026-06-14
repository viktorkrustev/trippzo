package com.trippzo.service;

import com.trippzo.model.Review;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Trip testTrip;
    private User reviewer;
    private User reviewee;
    private Review testReview;

    @BeforeEach
    void setUp() {
        reviewer = new User();
        reviewer.setId(1L);
        reviewer.setUsername("reviewer");

        reviewee = new User();
        reviewee.setId(2L);
        reviewee.setUsername("reviewee");

        testTrip = new Trip();
        testTrip.setId(1L);
        testTrip.setOrigin("Sofia");
        testTrip.setDestination("Plovdiv");
        testTrip.setSeatsTotal(4);
        testTrip.setPricePerSeat(BigDecimal.valueOf(15.00));
        testTrip.setDriver(reviewee);

        testReview = new Review();
        testReview.setId(1L);
        testReview.setTrip(testTrip);
        testReview.setReviewer(reviewer);
        testReview.setReviewee(reviewee);
        testReview.setRating(5);
        testReview.setComment("Great driver!");
        testReview.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testIsValidRating_Valid() {
        for (int rating = 1; rating <= 5; rating++) {
            assertTrue(reviewService.isValidRating(rating));
        }
    }

    @Test
    void testIsValidRating_TooLow() {
        assertFalse(reviewService.isValidRating(0));
    }

    @Test
    void testIsValidRating_TooHigh() {
        assertFalse(reviewService.isValidRating(6));
    }

    @Test
    void testCreateAndSaveReview_Success() {
        when(reviewRepository.existsByTripIdAndReviewerId(1L, 1L)).thenReturn(false);

        reviewService.createAndSaveReview(testTrip, reviewer, reviewee, 5, "Great driver!");

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testCreateAndSaveReview_InvalidRating() {
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createAndSaveReview(testTrip, reviewer, reviewee, 6, "Great!"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateAndSaveReview_AlreadyReviewed() {
        when(reviewRepository.existsByTripIdAndReviewerId(1L, 1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
            reviewService.createAndSaveReview(testTrip, reviewer, reviewee, 5, "Great!")
        );
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testGetReviewsForDriver() {
        List<Review> reviews = List.of(testReview);
        when(reviewRepository.findByRevieweeId(2L)).thenReturn(reviews);

        List<Review> result = reviewService.getReviewsForDriver(2L);

        assertEquals(1, result.size());
        assertEquals(5, result.getFirst().getRating());
    }

    @Test
    void testGetReviewsForTrip() {
        List<Review> reviews = List.of(testReview);
        when(reviewRepository.findByTripId(1L)).thenReturn(reviews);

        List<Review> result = reviewService.getReviewsForTrip(1L);

        assertEquals(1, result.size());
    }

    @Test
    void testGetAverageRatingForDriver_WithReviews() {
        Review review1 = new Review();
        review1.setRating(4);
        Review review2 = new Review();
        review2.setRating(5);
        Review review3 = new Review();
        review3.setRating(5);

        List<Review> reviews = List.of(review1, review2, review3);
        when(reviewRepository.findByRevieweeId(2L)).thenReturn(reviews);

        double average = reviewService.getAverageRatingForDriver(2L);

        assertEquals(4.7, average);
    }

    @Test
    void testGetAverageRatingForDriver_NoReviews() {
        when(reviewRepository.findByRevieweeId(2L)).thenReturn(List.of());

        double average = reviewService.getAverageRatingForDriver(2L);

        assertEquals(0.0, average);
    }

    @Test
    void testGetAverageRatingForDriver_SingleReview() {
        Review review = new Review();
        review.setRating(3);

        List<Review> reviews = List.of(review);
        when(reviewRepository.findByRevieweeId(2L)).thenReturn(reviews);

        double average = reviewService.getAverageRatingForDriver(2L);

        assertEquals(3.0, average);
    }

    @Test
    void testGetReviewCountForDriver() {
        when(reviewRepository.countByRevieweeId(2L)).thenReturn(5);

        int count = reviewService.getReviewCountForDriver(2L);

        assertEquals(5, count);
    }

    @Test
    void testGetReviewById() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        Optional<Review> result = reviewService.getReviewById(1L);

        assertTrue(result.isPresent());
        assertEquals(5, result.get().getRating());
    }

    @Test
    void testGetReviewById_NotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Review> result = reviewService.getReviewById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testHasUserReviewedTrip() {
        when(reviewRepository.existsByTripIdAndReviewerId(1L, 1L)).thenReturn(true);

        boolean result = reviewService.hasUserReviewedTrip(1L, 1L);

        assertTrue(result);
    }

    @Test
    void testHasUserReviewedTrip_NoReview() {
        when(reviewRepository.existsByTripIdAndReviewerId(1L, 1L)).thenReturn(false);

        boolean result = reviewService.hasUserReviewedTrip(1L, 1L);

        assertFalse(result);
    }

    @Test
    void testDeleteReview() {
        reviewService.deleteReview(1L);

        verify(reviewRepository, times(1)).deleteById(1L);
    }
}
