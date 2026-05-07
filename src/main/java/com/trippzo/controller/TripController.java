package com.trippzo.controller;

import com.trippzo.config.CustomUserDetails;
import com.trippzo.model.*;
import com.trippzo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Controller
public class TripController {

    @Autowired
    private TripService tripService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/trips/create")
    public String showCreateForm(Model model) {
        model.addAttribute("trip", new Trip());
        return "trip-create";
    }

    @PostMapping("/trips/create")
    public String createTrip(@Validated @ModelAttribute("trip") Trip trip, BindingResult bindingResult, Model model,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            String dateTimeString = trip.getDepartureDate() + " " + trip.getDepartureTime();
            LocalDateTime departureDateTime = LocalDateTime.parse(dateTimeString, formatter);
            trip.setDepartureDateTime(departureDateTime);
        } catch (DateTimeParseException e) {
            bindingResult.rejectValue("departureDate", "error.trip", "Невалидна дата или час.");
        }

        if (bindingResult.hasErrors()) {
            return "trip-create";
        }

        User currentUser = customUserDetails.getUser();
        trip.setDriver(currentUser);
        tripService.saveTrip(trip);
        return "redirect:/";
    }

    @GetMapping("/trips/search")
    public String searchTrips(@RequestParam(required = false) String from, @RequestParam(required = false) String to,
            @RequestParam(required = false) String date, @RequestParam(defaultValue = "0") int page, Model model) {

        Pageable pageable = PageRequest.of(page, 10,
                org.springframework.data.domain.Sort.by("departureDateTime").ascending());

        Page<Trip> tripPage = tripService.searchTrips(from, to, date, pageable);

        java.util.Map<Long, Double> driverRatings = new java.util.HashMap<>();
        for (Trip trip : tripPage.getContent()) {
            Long driverId = trip.getDriver().getId();
            if (!driverRatings.containsKey(driverId)) {
                driverRatings.put(driverId, reviewService.getAverageRatingForDriver(driverId));
            }
        }

        model.addAttribute("trips", tripPage.getContent());
        model.addAttribute("driverRatings", driverRatings); // Изпращаме мапа към Thymeleaf
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tripPage.getTotalPages());

        // Запазваме параметрите за филтриране
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("date", date);

        return "trip-search";
    }

    @GetMapping("/trips/{id}")
    public String viewTrip(@PathVariable Long id, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Trip trip = tripService.getTripById(id);
        if (trip == null) {
            return "redirect:/trips/search";
        }
        List<Message> messages = chatService.getMessagesForTrip(id);
        int seatsAvailable = trip.getSeatsTotal() - (trip.getPassengers() != null ? trip.getPassengers().size() : 0);
        List<Review> reviews = reviewService.getReviewsForTrip(id);
        double driverRating = reviewService.getAverageRatingForDriver(trip.getDriver().getId());
        int reviewCount = reviewService.getReviewCountForDriver(trip.getDriver().getId());

        model.addAttribute("trip", trip);
        model.addAttribute("messages", messages);
        model.addAttribute("seatsAvailable", seatsAvailable);
        model.addAttribute("currentUsername", userDetails.getUsername());
        model.addAttribute("reviews", reviews);
        model.addAttribute("driverRating", driverRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("driverLevel", trip.getDriver().getDriverLevel());
        model.addAttribute("hasReviewed", reviewService.hasUserReviewedTrip(id, userDetails.getUser().getId()));

        Optional<Notification> seatRequest = notificationService.findSeatRequestNotification(id,
                userDetails.getUser().getId());
        if (seatRequest.isPresent()) {
            Notification notification = seatRequest.get();
            model.addAttribute("seatRequestStatus", notification.getStatus());
            model.addAttribute("hasSeatRequest", true);
        } else {
            model.addAttribute("hasSeatRequest", false);
        }

        return "trip-details";
    }

    @PostMapping("/trips/{id}/chat")
    public String sendMessage(@PathVariable Long id, @RequestParam String message,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User sender = userDetails.getUser();
        Trip trip = tripService.getTripById(id);

        if (trip == null || trip.getDriver() == null) {
            return "redirect:/trips/" + id + "#chat-box";
        }

        String receiverUsername = trip.getDriver().getUsername();

        chatService.saveMessage(id, sender.getUsername(), message, receiverUsername);
        return "redirect:/trips/" + id + "#chat-box";
    }

    @PostMapping("/trips/{tripId}/book")
    public String requestSeat(@PathVariable("tripId") Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Trip trip = tripService.getTripById(tripId);
        if (trip == null) {
            return "redirect:/trips/search";
        }

        User passenger = userDetails.getUser();

        notificationService.createSeatRequestNotification(trip, passenger);

        return "redirect:/trips/" + tripId + "?success=true";
    }

    @PostMapping("/trips/{tripId}/delete")
    public String deleteTrip(@PathVariable Long tripId, @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Optional<Trip> optionalTrip = tripService.findById(tripId);
        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();

            if (trip.getDriver().getId().equals(userDetails.getUser().getId())) {
                tripService.deleteTrip(trip);
                redirectAttributes.addFlashAttribute("successMessage", "Пътуването е изтрито успешно.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Нямате права да изтриете това пътуване.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Пътуването не съществува.");
        }

        return "redirect:/profile";
    }
}
