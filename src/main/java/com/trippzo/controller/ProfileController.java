package com.trippzo.controller;

import com.trippzo.config.CustomUserDetails;
import com.trippzo.model.User;
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

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private TripService tripService;

    private final String UPLOAD_DIR = "D:/trippzo/uploads/avatars/";

    @GetMapping
    public String profilePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();

        model.addAttribute("user", user);
        model.addAttribute("totalTrips", tripService.getTripsByUser(user));
        model.addAttribute("driverTrips", tripService.getTripsAsDriver(user));
        model.addAttribute("passengerTrips", tripService.getTripsAsPassenger(user));

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
        User user = userDetails.getUser();

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = user.getId() + "_" + file.getOriginalFilename();
        File dest = new File(UPLOAD_DIR + fileName);

        file.transferTo(dest);

        user.setAvatarUrl("/uploads/avatars/" + fileName);
        userService.saveUser(user);

        return "redirect:/profile";
    }

}
