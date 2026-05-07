package com.trippzo.controller;

import com.trippzo.config.CustomUserDetails;
import com.trippzo.model.Notification;
import com.trippzo.model.Trip;
import com.trippzo.model.TripPassenger;
import com.trippzo.model.User;
import com.trippzo.repository.TripPassengerRepository;
import com.trippzo.service.NotificationService;
import com.trippzo.service.TripService;
import com.trippzo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TripService tripService;

    @Autowired
    private UserService userService;

    @Autowired
    private TripPassengerRepository tripPassengerRepository;

    @GetMapping
    public String getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        List<Notification> notifications = notificationService.getAllNotifications(user.getId());
        model.addAttribute("notifications", notifications);
        return "notifications";
    }

    @PostMapping("/{id}/accept")
    public String acceptNotification(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        Optional<Notification> optionalNotification = notificationService.findById(id);

        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            Trip trip = notification.getTrip();
            User passenger = notification.getSender();

            Optional<TripPassenger> existingPassenger = tripPassengerRepository.findByTripIdAndUserId(trip.getId(),
                    passenger.getId());
            if (existingPassenger.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Потребителят вече е пътник на това пътуване!");
                return "redirect:/notifications";
            }

            int seatsAvailable = trip.getSeatsTotal() - tripPassengerRepository.countByTripId(trip.getId());

            if (seatsAvailable > 0) {
                TripPassenger tripPassenger = new TripPassenger();
                tripPassenger.setTrip(trip);
                tripPassenger.setUser(passenger);
                tripPassengerRepository.save(tripPassenger);

                notificationService.acceptSeatRequest(id);

                redirectAttributes.addFlashAttribute("successMessage", "Място потвърдено!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Няма свободни места!");
                notificationService.rejectSeatRequest(id);
            }
        }

        return "redirect:/notifications";
    }

    @PostMapping("/{id}/reject")
    public String rejectNotification(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        Optional<Notification> optionalNotification = notificationService.findById(id);

        if (optionalNotification.isPresent()) {
            notificationService.rejectSeatRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Заявката е отхвърлена!");
        }

        return "redirect:/notifications";
    }

    @PostMapping("/{id}/mark-read")
    @ResponseBody
    public String markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return "success";
    }

    @PostMapping("/{id}/delete")
    public String deleteNotification(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Notification> optionalNotification = notificationService.findById(id);

        if (optionalNotification.isPresent()) {
            notificationService.deleteNotification(id);
            redirectAttributes.addFlashAttribute("successMessage", "Известието беше изтрито.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Известието не е намерено.");
        }

        return "redirect:/notifications";
    }

    @PostMapping("/delete-all")
    public String deleteAllNotifications(@AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        notificationService.deleteAllNotifications(userDetails.getUser().getId());
        redirectAttributes.addFlashAttribute("successMessage", "Всички известия бяха изтрити.");
        return "redirect:/notifications";
    }

    @GetMapping("/unread/count")
    @ResponseBody
    public int getUnreadNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationService.countUnread(userDetails.getUser().getId());
    }
}
