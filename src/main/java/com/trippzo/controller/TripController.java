package com.trippzo.controller;

import com.trippzo.model.Notification;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.model.dto.TripCreateDTO;
import com.trippzo.service.ChatService;
import com.trippzo.service.NotificationService;
import com.trippzo.service.ReviewService;
import com.trippzo.service.TripService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class TripController extends BaseController {

    private final TripService tripService;
    private final ChatService chatService;
    private final ReviewService reviewService;
    private final NotificationService notificationService;

    public TripController(TripService tripService,
                          ChatService chatService,
                          ReviewService reviewService,
                          NotificationService notificationService) {
        this.tripService = tripService;
        this.chatService = chatService;
        this.reviewService = reviewService;
        this.notificationService = notificationService;
    }

    @GetMapping("/trips/create")
    public String showCreateForm(Model model, HttpServletRequest request) {
        request.getSession(true);

        model.addAttribute("tripDto", new TripCreateDTO());

        return "trip-create";
    }

    @PostMapping("/trips/create")
    public String createTrip(@Valid @ModelAttribute("tripDto") TripCreateDTO tripDto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal Object principal) {

        User user = resolveUser(principal);

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
    public String searchTrips(@RequestParam(required = false) String from,
                              @RequestParam(required = false) String to,
                              @RequestParam(required = false) String date,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("departureDateTime").ascending());
        Page<Trip> tripPage = tripService.searchTrips(from, to, date, pageable);

        Map<Long, Double> driverRatings = new HashMap<>();
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
    public String viewTrip(@PathVariable Long id, Model model,
                           @AuthenticationPrincipal Object principal) {
        Trip trip = tripService.getTripById(id);
        if (trip == null || trip.getId() == null) {
            return "redirect:/trips/search";
        }

        User currentUser = resolveUser(principal);

        boolean isDriver = currentUser != null &&
                trip.getDriver() != null &&
                trip.getDriver().getId().equals(currentUser.getId());

        int passengersCount = (trip.getPassengers() != null) ? trip.getPassengers().size() : 0;
        int seatsAvailable = Math.max(0, trip.getSeatsTotal() - passengersCount);

        double driverRating = reviewService.getAverageRatingForDriver(trip.getDriver().getId());
        int reviewCount = reviewService.getReviewCountForDriver(trip.getDriver().getId());

        model.addAttribute("trip", trip);
        model.addAttribute("isDriver", isDriver);
        model.addAttribute("seatsAvailable", seatsAvailable);
        model.addAttribute("driverRating", driverRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("driverLevel", trip.getDriver().getDriverLevel());
        model.addAttribute("messages", chatService.getMessagesForTrip(id));
        model.addAttribute("reviews", reviewService.getReviewsForTrip(id));

        if (currentUser != null) {
            model.addAttribute("currentUsername", currentUser.getUsername());
            model.addAttribute("hasReviewed", reviewService.hasUserReviewedTrip(id, currentUser.getId()));

            Optional<Notification> seatRequest = notificationService.findSeatRequestNotification(id, currentUser.getId());

            model.addAttribute("hasSeatRequest", seatRequest.isPresent());
            model.addAttribute("seatRequestStatus", seatRequest.map(Notification::getStatus).orElse(null));
        } else {
            model.addAttribute("currentUsername", null);
            model.addAttribute("hasReviewed", false);
            model.addAttribute("hasSeatRequest", false);
            model.addAttribute("seatRequestStatus", null); // Важно за избягване на грешки в HTML
        }

        return "trip-details";
    }

    @PostMapping("/trips/{id}/chat")
    public String sendMessage(@PathVariable Long id,
                              @RequestParam String message,
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

        chatService.saveMessage(id, sender.getUsername(), message, trip.getDriver().getUsername());
        return "redirect:/trips/" + id + "#chat-box";
    }

    @PostMapping("/trips/{tripId}/delete")
    public String deleteTrip(@PathVariable Long tripId,
                             @AuthenticationPrincipal Object principal,
                             RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Trip> optionalTrip = tripService.findById(tripId);
        if (optionalTrip.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пътуването не съществува.");
            return "redirect:/profile";
        }

        Trip trip = optionalTrip.get();
        if (!trip.getDriver().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Нямате права да изтриете това пътуване.");
            return "redirect:/profile";
        }

        tripService.deleteTrip(trip);
        redirectAttributes.addFlashAttribute("successMessage", "Пътуването е изтрито успешно.");
        return "redirect:/profile";
    }
}