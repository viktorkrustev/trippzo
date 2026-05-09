package com.trippzo.controller;

import com.trippzo.model.Review;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.service.ReviewService;
import com.trippzo.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController extends BaseController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private TripService tripService;

    @PostMapping("/{tripId}/add")
    public String addReview(@PathVariable Long tripId, @RequestParam int rating,
            @RequestParam(required = false) String comment, @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes) {

        User currentUser = resolveUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Trip> tripOptional = tripService.findById(tripId);
        if (tripOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пътуването не съществува.");
            return "redirect:/trips/search";
        }

        Trip trip = tripOptional.get();

        if (reviewService.hasUserReviewedTrip(tripId, currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Вече сте оценили това пътуване.");
            return "redirect:/trips/" + tripId;
        }

        if (rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("errorMessage", "Оценката трябва да е между 1 и 5.");
            return "redirect:/trips/" + tripId;
        }

        Review review = new Review();
        review.setTrip(trip);
        review.setReviewer(currentUser);
        review.setReviewee(trip.getDriver());
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        reviewService.saveReview(review);
        redirectAttributes.addFlashAttribute("successMessage", "Вашата оценка беше записана успешно.");
        return "redirect:/trips/" + tripId;
    }

    @PostMapping("/{reviewId}/delete")
    public String deleteReview(@PathVariable Long reviewId, @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes) {

        User currentUser = resolveUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Review> reviewOptional = reviewService.getReviewById(reviewId);
        if (reviewOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Оценката не съществува.");
            return "redirect:/profile";
        }

        Review review = reviewOptional.get();

        if (!review.getReviewer().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нямате право да изтриете тази оценка.");
            return "redirect:/profile";
        }

        Long tripId = review.getTrip().getId();
        reviewService.deleteReview(reviewId);

        redirectAttributes.addFlashAttribute("successMessage", "Оценката беше изтрита.");
        return "redirect:/trips/" + tripId;
    }
}
