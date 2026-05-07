package com.trippzo.controller;

import com.trippzo.config.CustomUserDetails;
import com.trippzo.model.Review;
import com.trippzo.model.User;
import com.trippzo.service.CloudinaryService;
import com.trippzo.service.ReviewService;
import com.trippzo.service.TripService;
import com.trippzo.service.UserService;
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
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private TripService tripService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CloudinaryService cloudinaryService;


    @GetMapping
    public String profilePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();

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
    public String editProfile(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam String fullName,
            @RequestParam String email) {
        User user = userDetails.getUser();
        user.setFullName(fullName);
        user.setEmail(email);
        userService.saveUser(user);
        return "redirect:/profile";
    }

    @PostMapping("/upload-avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file,
                               @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {

        if (file.isEmpty()) {
            return "redirect:/profile?error=empty";
        }

        // 1. Качваме в облака
        String imageUrl = cloudinaryService.uploadImage(file);

        // 2. Взимаме потребителя и обновяваме URL-а
        User user = userDetails.getUser();
        user.setAvatarUrl(imageUrl);
        userService.saveUser(user);

        return "redirect:/profile";
    }

}
