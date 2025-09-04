package com.trippzo.controller;

import com.trippzo.config.CustomUserDetails;
import com.trippzo.model.Message;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.service.ChatService;
import com.trippzo.service.TripService;
import com.trippzo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class TripController {

    @Autowired
    private TripService tripService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @GetMapping("/trips/create")
    public String showCreateForm(Model model) {
        model.addAttribute("trip", new Trip());
        return "trip-create";
    }

    @PostMapping("/trips/create")
    public String createTrip(@Validated @ModelAttribute("trip") Trip trip,
                             BindingResult bindingResult,
                             Model model,
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
    public String searchTrips(@RequestParam(required = false) String from,
                              @RequestParam(required = false) String to,
                              @RequestParam(required = false) String date,
                              Model model) {
        List<Trip> trips = tripService.searchTrips(from, to, date);
        Map<Long, Double> driverRatings = new HashMap<>();

        model.addAttribute("trips", trips);
        model.addAttribute("driverRatings", driverRatings);
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

        model.addAttribute("trip", trip);
        model.addAttribute("messages", messages);
        model.addAttribute("seatsAvailable", seatsAvailable);
        model.addAttribute("currentUsername", userDetails.getUsername());

        // Тук трябва да добавиш driverRatings
        Map<Long, Double> driverRatings = new HashMap<>();

        model.addAttribute("driverRatings", driverRatings);

        model.addAttribute("currentUsername", userDetails.getUsername());


        return "trip-details";
    }



    @PostMapping("/trips/{id}/chat")
    public String sendMessage(@PathVariable Long id,
                              @RequestParam String message,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        User sender = userDetails.getUser();
        Trip trip = tripService.getTripById(id);

        if (trip == null || trip.getDriver() == null) {
            return "redirect:/trips/" + id + "#chat-box";
        }

        String receiverUsername;

        if (sender.getUsername().equals(trip.getDriver().getUsername())) {
            // Ако шофьорът пише, намери първия пътник или някакъв логика за това
            // (тук ще трябва да доразвиеш логиката за реална комуникация с конкретен пасажер)
            receiverUsername = "тук-постави-юзърнейм-на-получателя"; // <-- фиксирай логиката!
        } else {
            // Ако пасажерът пише → до шофьора
            receiverUsername = trip.getDriver().getUsername();
        }

        chatService.saveMessage(id, sender.getUsername(), message, receiverUsername);
        return "redirect:/trips/" + id + "#chat-box";
    }



    @PostMapping("/trips/{tripId}/book")
    public String requestSeat(@PathVariable("tripId") Long tripId, Principal principal) {
        Trip trip = tripService.getTripById(tripId);
        if (trip == null) {
            return "redirect:/trips";
        }

        chatService.saveMessage(
                tripId,
                principal.getName(),
                "Искам да заявя място за това пътуване.",
                trip.getDriver().getUsername()
        );

        return "redirect:/trips/" + tripId + "?success=true";
    }

    @PostMapping("/trips/{tripId}/delete")
    public String deleteTrip(@PathVariable Long tripId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {

        Optional<Trip> optionalTrip = tripService.findById(tripId); // използвай service, а не директно repository
        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();

            // проверка дали текущия потребител е шофьор
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