package com.trippzo.controller;

import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.service.TripService;
import com.trippzo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.trippzo.config.CustomUserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Optional;

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
    public String editProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam String fullName,
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

        // Създава папката, ако не съществува
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Име на файла: userId_originalFileName
        String fileName = user.getId() + "_" + file.getOriginalFilename();
        File dest = new File(UPLOAD_DIR + fileName);

        // Записва файла
        file.transferTo(dest);

        // Запазва пътя към аватара в базата данни (за Thymeleaf ще ползваме относителен URL)
        user.setAvatarUrl("/uploads/avatars/" + fileName);
        userService.saveUser(user);

        return "redirect:/profile";
    }




}
