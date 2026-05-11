package com.trippzo.controller;

import com.trippzo.model.*;
import com.trippzo.model.dto.TripCreateDTO;
import com.trippzo.service.ChatService;
import com.trippzo.service.NotificationService;
import com.trippzo.service.ReviewService;
import com.trippzo.service.TripService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class TripController extends BaseController {

    @Autowired
    private TripService tripService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/trips/create")
    public String showCreateForm(Model model) {
        model.addAttribute("tripDto", new TripCreateDTO());
        return "trip-create";
    }

    @PostMapping("/trips/create")
    public String createTrip(@Valid @ModelAttribute("tripDto") TripCreateDTO tripDto, BindingResult bindingResult,
            @AuthenticationPrincipal Object principal) {

        User user = resolveUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "trip-create";
        }

        try {
            tripService.createNewTrip(tripDto, user);
        } catch (RuntimeException e) {
            bindingResult.rejectValue("departureDate", "error.trip", e.getMessage());
            return "trip-create";
        }

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
        model.addAttribute("driverRatings", driverRatings);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tripPage.getTotalPages());
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("date", date);

        return "trip-search";
    }

    @GetMapping("/trips/{id}")
    public String viewTrip(@PathVariable Long id, Model model, @AuthenticationPrincipal Object principal) {
        Trip trip = tripService.getTripById(id);
        if (trip == null) {
            return "redirect:/trips/search";
        }

        User currentUser = resolveUser(principal);
        boolean isDriver = currentUser != null && trip.getDriver().getId().equals(currentUser.getId());

        List<Message> messages = chatService.getMessagesForTrip(id);
        int seatsAvailable = trip.getSeatsTotal() - (trip.getPassengers() != null ? trip.getPassengers().size() : 0);
        List<Review> reviews = reviewService.getReviewsForTrip(id);
        double driverRating = reviewService.getAverageRatingForDriver(trip.getDriver().getId());
        int reviewCount = reviewService.getReviewCountForDriver(trip.getDriver().getId());

        model.addAttribute("trip", trip);
        model.addAttribute("isDriver", isDriver);
        model.addAttribute("messages", messages);
        model.addAttribute("seatsAvailable", seatsAvailable);
        model.addAttribute("currentUsername", currentUser != null ? currentUser.getUsername() : null);
        model.addAttribute("reviews", reviews);
        model.addAttribute("driverRating", driverRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("driverLevel", trip.getDriver().getDriverLevel());
        model.addAttribute("hasReviewed",
                currentUser != null && reviewService.hasUserReviewedTrip(id, currentUser.getId()));

        if (currentUser != null) {
            Optional<Notification> seatRequest = notificationService.findSeatRequestNotification(id,
                    currentUser.getId());
            if (seatRequest.isPresent()) {
                Notification notification = seatRequest.get();
                model.addAttribute("seatRequestStatus", notification.getStatus());
                model.addAttribute("hasSeatRequest", true);
            } else {
                model.addAttribute("hasSeatRequest", false);
            }
        } else {
            model.addAttribute("hasSeatRequest", false);
        }

        return "trip-details";
    }

    @PostMapping("/trips/{id}/chat")
    public String sendMessage(@PathVariable Long id, @RequestParam String message,
            @AuthenticationPrincipal Object principal) {
        User sender = resolveUser(principal);
        if (sender == null) {
            return "redirect:/login";
        }

        Trip trip = tripService.getTripById(id);

        if (trip == null || trip.getDriver() == null) {
            return "redirect:/trips/" + id + "#chat-box";
        }

        if (trip.getDriver().getId().equals(sender.getId())) {
            return "redirect:/trips/" + id;
        }

        String receiverUsername = trip.getDriver().getUsername();
        chatService.saveMessage(id, sender.getUsername(), message, receiverUsername);
        return "redirect:/trips/" + id + "#chat-box";
    }

    @PostMapping("/trips/{tripId}/book")
    public String requestSeat(@PathVariable("tripId") Long tripId, @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes) {
        User passenger = resolveUser(principal);
        if (passenger == null) {
            return "redirect:/login";
        }

        Trip trip = tripService.getTripById(tripId);

        if (trip == null) {
            return "redirect:/trips/search";
        }

        if (trip.getDriver().getId().equals(passenger.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Не можете да запазвате място в собственото си пътуване.");
            return "redirect:/trips/" + tripId;
        }

        Optional<Notification> existingRequest = notificationService.findSeatRequestNotification(tripId,
                passenger.getId());
        if (existingRequest.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Вече сте изпратили заявка за това пътуване.");
            return "redirect:/trips/" + tripId;
        }

        notificationService.createSeatRequestNotification(trip, passenger);

        return "redirect:/trips/" + tripId + "?success=true";
    }

    @PostMapping("/trips/{tripId}/delete")
    public String deleteTrip(@PathVariable Long tripId, @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Trip> optionalTrip = tripService.findById(tripId);
        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();

            if (trip.getDriver().getId().equals(user.getId())) {
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
