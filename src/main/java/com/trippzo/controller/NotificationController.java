package com.trippzo.controller;

import com.trippzo.model.Notification;
import com.trippzo.model.Trip;
import com.trippzo.model.TripPassenger;
import com.trippzo.model.User;
import com.trippzo.repository.TripPassengerRepository;
import com.trippzo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/notifications")
public class NotificationController extends BaseController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TripPassengerRepository tripPassengerRepository;

    @GetMapping
    public String getNotifications(@AuthenticationPrincipal Object principal, Model model) {
        User user = resolveUser(principal);
        if (user == null)
            return "redirect:/login";

        List<Notification> notifications = notificationService.getAllNotifications(user.getId());
        model.addAttribute("notifications", notifications);
        return "notifications";
    }

    @PostMapping("/{id}/accept")
    public String acceptNotification(@PathVariable Long id, @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        if (user == null)
            return "redirect:/login";

        Optional<Notification> optionalNotification = notificationService.findById(id);

        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();

            if (!notification.getRecipient().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Нямате права над това известие!");
                return "redirect:/notifications";
            }

            Trip trip = notification.getTrip();
            User passenger = notification.getSender();

            Optional<TripPassenger> existingPassenger = tripPassengerRepository.findByTripIdAndUserId(trip.getId(),
                    passenger.getId());
            if (existingPassenger.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Потребителят вече е пътник в това пътуване!");
                notificationService.deleteNotification(id);
                return "redirect:/notifications";
            }

            int currentPassengers = tripPassengerRepository.countByTripId(trip.getId());
            int seatsAvailable = trip.getSeatsTotal() - currentPassengers;

            if (seatsAvailable > 0) {
                TripPassenger tripPassenger = new TripPassenger();
                tripPassenger.setTrip(trip);
                tripPassenger.setUser(passenger);
                tripPassengerRepository.save(tripPassenger);

                notificationService.acceptSeatRequest(id);
                redirectAttributes.addFlashAttribute("successMessage", "Мястото е потвърдено успешно!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Няма свободни места за това пътуване!");
                notificationService.rejectSeatRequest(id);
            }
        }
        return "redirect:/notifications";
    }

    @PostMapping("/{id}/reject")
    public String rejectNotification(@PathVariable Long id, @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        if (user == null)
            return "redirect:/login";

        Optional<Notification> optionalNotification = notificationService.findById(id);

        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            if (!notification.getRecipient().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Нямате права над това известие!");
                return "redirect:/notifications";
            }

            notificationService.rejectSeatRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Заявката беше отхвърлена.");
        }
        return "redirect:/notifications";
    }

    @PostMapping("/{id}/mark-read")
    @ResponseBody
    public ResponseEntity<String> markAsRead(@PathVariable Long id, @AuthenticationPrincipal Object principal) {
        User user = resolveUser(principal);
        Optional<Notification> notification = notificationService.findById(id);

        if (user != null && notification.isPresent()
                && notification.get().getRecipient().getId().equals(user.getId())) {
            notificationService.markAsRead(id);
            return ResponseEntity.ok("success");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("error");
    }

    @PostMapping("/{id}/delete")
    public String deleteNotification(@PathVariable Long id, @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        if (user == null)
            return "redirect:/login";

        Optional<Notification> optionalNotification = notificationService.findById(id);

        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            if (!notification.getRecipient().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Нямате права да изтриете това известие!");
                return "redirect:/notifications";
            }

            notificationService.deleteNotification(id);
            redirectAttributes.addFlashAttribute("successMessage", "Известието беше изтрито.");
        }
        return "redirect:/notifications";
    }

    @PostMapping("/delete-all")
    public String deleteAllNotifications(@AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        if (user != null) {
            notificationService.deleteAllNotifications(user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Всички известия бяха изтрити.");
        }
        return "redirect:/notifications";
    }

    @GetMapping("/unread/count")
    @ResponseBody
    public int getUnreadNotifications(@AuthenticationPrincipal Object principal) {
        User user = resolveUser(principal);
        if (user == null)
            return 0;
        return notificationService.countUnread(user.getId());
    }
}
