package com.trippzo.service;

import com.trippzo.model.Notification;
import com.trippzo.model.Trip;
import com.trippzo.model.TripPassenger;
import com.trippzo.model.User;
import com.trippzo.repository.TripPassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private TripPassengerRepository tripPassengerRepository;

    @Autowired
    private NotificationService notificationService;

    public boolean isUserAlreadyPassenger(Long tripId, Long userId) {
        return tripPassengerRepository.findByTripIdAndUserId(tripId, userId).isPresent();
    }

    public int getAvailableSeats(Trip trip) {
        int currentPassengers = tripPassengerRepository.countByTripId(trip.getId());
        return trip.getSeatsTotal() - currentPassengers;
    }

    public boolean hasAvailableSeats(Trip trip) {
        return getAvailableSeats(trip) > 0;
    }

    @Transactional(readOnly = true)
    public boolean canRequestSeat(Trip trip, User passenger) {
        if (trip == null || trip.getDriver() == null || passenger == null) {
            throw new IllegalArgumentException("Trip, driver and passenger cannot be null");
        }

        if (trip.getDriver().getId().equals(passenger.getId())) {
            return false; // Driver cannot request seat in their own trip
        }

        if (notificationService.hasExistingSeatRequest(trip.getId(), passenger.getId())) {
            return false; // Passenger already has a pending request
        }

        return true;
    }

    @Transactional
    private void createAndSavePassenger(Trip trip, User user) {
        TripPassenger tripPassenger = new TripPassenger();
        tripPassenger.setTrip(trip);
        tripPassenger.setUser(user);
        tripPassengerRepository.save(tripPassenger);
    }


    @Transactional
    public boolean acceptSeatRequest(Long notificationId) {
        Optional<Notification> optionalNotification = notificationService.findById(notificationId);

        if (optionalNotification.isEmpty()) {
            return false;
        }

        Notification notification = optionalNotification.get();
        Trip trip = notification.getTrip();
        User passenger = notification.getSender();

        if (isUserAlreadyPassenger(trip.getId(), passenger.getId())) {
            notificationService.deleteNotification(notificationId);
            return false;
        }

        if (!hasAvailableSeats(trip)) {
            notificationService.rejectSeatRequest(notificationId);
            return false;
        }

        createAndSavePassenger(trip, passenger);
        notificationService.acceptSeatRequest(notificationId);

        return true;
    }

    @Transactional
    public void rejectSeatRequest(Long notificationId) {
        notificationService.rejectSeatRequest(notificationId);
    }
}



