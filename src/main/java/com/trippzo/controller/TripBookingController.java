package com.trippzo.controller;

import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.service.NotificationService;
import com.trippzo.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/trips/{tripId}/booking")
public class TripBookingController extends BaseController {

    @Autowired
    private TripService tripService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/request")
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

        Optional<com.trippzo.model.Notification> existingRequest = notificationService.findSeatRequestNotification(tripId,
                passenger.getId());
        if (existingRequest.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Вече сте изпратили заявка за това пътуване.");
            return "redirect:/trips/" + tripId;
        }

        notificationService.createSeatRequestNotification(trip, passenger);

        return "redirect:/trips/" + tripId + "?success=true";
    }
}

