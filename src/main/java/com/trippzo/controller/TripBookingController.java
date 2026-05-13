package com.trippzo.controller;

import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.service.BookingService;
import com.trippzo.service.NotificationService;
import com.trippzo.service.TripService;
import com.trippzo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/trips/{tripId}/booking")
public class TripBookingController extends BaseController {

    private final TripService tripService;
    private final NotificationService notificationService;
    private final BookingService bookingService;

    public TripBookingController(UserService userService, TripService tripService,
            NotificationService notificationService, BookingService bookingService) {
        super(userService);
        this.tripService = tripService;
        this.notificationService = notificationService;
        this.bookingService = bookingService;
    }

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

        if (!bookingService.canRequestSeat(trip, passenger)) {
            if (trip.getDriver().getId().equals(passenger.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Не можете да запазвате място в собственото си пътуване.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Вече сте изпратили заявка за това пътуване.");
            }
            return "redirect:/trips/" + tripId;
        }

        notificationService.createSeatRequestNotification(trip, passenger);

        return "redirect:/trips/" + tripId + "?success=true";
    }
}
