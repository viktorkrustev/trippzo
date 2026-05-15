package com.trippzo.controller;

import com.trippzo.model.Review;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.service.ReviewService;
import com.trippzo.service.TripService;
import com.trippzo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController extends BaseController {

    private final ReviewService reviewService;
    private final TripService tripService;

    public ReviewController(UserService userService, ReviewService reviewService, TripService tripService) {
        super(userService);
        this.reviewService = reviewService;
        this.tripService = tripService;
    }

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

        try {
            reviewService.createAndSaveReview(trip, currentUser, trip.getDriver(), rating, comment);
            redirectAttributes.addFlashAttribute("successMessage", "Вашата оценка беше записана успешно.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

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
