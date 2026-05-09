package com.trippzo.controller;

import com.trippzo.model.Review;
import com.trippzo.model.User;
import com.trippzo.service.CloudinaryService;
import com.trippzo.service.ReviewService;
import com.trippzo.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController extends BaseController {

    @Autowired
    private TripService tripService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping
    public String profilePage(@AuthenticationPrincipal Object principal, Model model) {
        User user = resolveUser(principal);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("totalTrips", tripService.getTripsByUser(user));
        model.addAttribute("driverTrips", tripService.getTripsAsDriver(user));
        model.addAttribute("passengerTrips", tripService.getTripsAsPassenger(user));

        List<Review> driverReviews = reviewService.getReviewsForDriver(user.getId());
        double averageRating = reviewService.getAverageRatingForDriver(user.getId());
        model.addAttribute("driverReviews", driverReviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("reviewCount", driverReviews.size());

        return "profile";
    }

    @PostMapping("/edit")
    public String editProfile(@AuthenticationPrincipal Object principal, @RequestParam String fullName,
            @RequestParam String email) {
        User user = resolveUser(principal);
        if (user != null) {
            user.setFullName(fullName);
            user.setEmail(email);
            userService.saveUser(user);
        }
        return "redirect:/profile";
    }

    @PostMapping("/upload-avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file, @AuthenticationPrincipal Object principal)
            throws IOException {

        if (file.isEmpty()) {
            return "redirect:/profile?error=empty";
        }

        User user = resolveUser(principal);
        if (user != null) {
            String imageUrl = cloudinaryService.uploadImage(file);

            user.setAvatarUrl(imageUrl);
            userService.saveUser(user);
        }

        return "redirect:/profile";
    }
}
