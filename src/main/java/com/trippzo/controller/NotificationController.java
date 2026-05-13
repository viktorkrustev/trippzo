package com.trippzo.controller;

import com.trippzo.model.Notification;
import com.trippzo.model.User;
import com.trippzo.service.BookingService;
import com.trippzo.service.NotificationService;
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

    private final NotificationService notificationService;
    private final BookingService bookingService;

    public NotificationController(NotificationService notificationService,
                                  BookingService bookingService) {
        this.notificationService = notificationService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public String getNotifications(@AuthenticationPrincipal Object principal, Model model) {
        User user = resolveUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        List<Notification> notifications = notificationService.getAllNotifications(user.getId());
        model.addAttribute("notifications", notifications);
        return "notifications";
    }

    @PostMapping("/{id}/accept")
    public String acceptNotification(@PathVariable Long id,
                                     @AuthenticationPrincipal Object principal,
                                     RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Notification> optionalNotification = notificationService.findById(id);
        if (optionalNotification.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Известието не съществува!");
            return "redirect:/notifications";
        }

        Notification notification = optionalNotification.get();
        if (!notification.getRecipient().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нямате права над това известие!");
            return "redirect:/notifications";
        }

        boolean accepted = bookingService.acceptSeatRequest(id);
        if (accepted) {
            redirectAttributes.addFlashAttribute("successMessage", "Мястото е потвърдено успешно!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Няма свободни места за това пътуване!");
        }

        return "redirect:/notifications";
    }

    @PostMapping("/{id}/reject")
    public String rejectNotification(@PathVariable Long id,
                                     @AuthenticationPrincipal Object principal,
                                     RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Notification> optionalNotification = notificationService.findById(id);
        if (optionalNotification.isEmpty()) {
            return "redirect:/notifications";
        }

        Notification notification = optionalNotification.get();
        if (!notification.getRecipient().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нямате права над това известие!");
            return "redirect:/notifications";
        }

        notificationService.rejectSeatRequest(id);
        redirectAttributes.addFlashAttribute("successMessage", "Заявката беше отхвърлена.");
        return "redirect:/notifications";
    }

    @PostMapping("/{id}/mark-read")
    @ResponseBody
    public ResponseEntity<String> markAsRead(@PathVariable Long id,
                                             @AuthenticationPrincipal Object principal) {
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
    public String deleteNotification(@PathVariable Long id,
                                     @AuthenticationPrincipal Object principal,
                                     RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Notification> optionalNotification = notificationService.findById(id);
        if (optionalNotification.isEmpty()) {
            return "redirect:/notifications";
        }

        Notification notification = optionalNotification.get();
        if (!notification.getRecipient().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нямате права да изтриете това известие!");
            return "redirect:/notifications";
        }

        notificationService.deleteNotification(id);
        redirectAttributes.addFlashAttribute("successMessage", "Известието беше изтрито.");
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
        if (user == null) {
            return 0;
        }
        return notificationService.countUnread(user.getId());
    }
}