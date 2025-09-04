package com.trippzo.service;

import com.trippzo.model.Trip;
import com.trippzo.model.TripPassenger;
import com.trippzo.model.User;
import com.trippzo.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TripService {

    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public Trip saveTrip(Trip trip) {
        return tripRepository.save(trip);
    }

    public List<Trip> searchTrips(String origin, String destination, String dateString) {
        boolean hasOrigin = origin != null && !origin.isBlank();
        boolean hasDestination = destination != null && !destination.isBlank();
        boolean hasDate = dateString != null && !dateString.isBlank();

        LocalDateTime start = LocalDateTime.MIN;
        LocalDateTime end = LocalDateTime.MAX;

        if (hasDate) {
            LocalDate date = LocalDate.parse(dateString);
            start = date.atStartOfDay();
            end = date.atTime(LocalTime.MAX);
        }

        if (hasOrigin && hasDestination && hasDate) {
            return tripRepository.findByOriginContainingIgnoreCaseAndDestinationContainingIgnoreCaseAndDepartureDateTimeBetween(
                    origin, destination, start, end);
        } else if (hasOrigin && hasDestination) {
            return tripRepository.findByOriginContainingIgnoreCaseAndDestinationContainingIgnoreCase(
                    origin, destination);
        } else if (hasOrigin && hasDate) {
            return tripRepository.findByOriginContainingIgnoreCaseAndDepartureDateTimeBetween(
                    origin, start, end);
        } else if (hasDestination && hasDate) {
            return tripRepository.findByDestinationContainingIgnoreCaseAndDepartureDateTimeBetween(
                    destination, start, end);
        } else if (hasOrigin) {
            return tripRepository.findByOriginContainingIgnoreCase(origin);
        } else if (hasDestination) {
            return tripRepository.findByDestinationContainingIgnoreCase(destination);
        } else if (hasDate) {
            return tripRepository.findByDepartureDateTimeBetween(start, end);
        } else {
            return tripRepository.findAll();
        }
    }


    public List<Trip> getTripsByDriver(User driver) {
        return tripRepository.findByDriver(driver);
    }

    public List<Trip> getTripsByPassenger(User passenger) {
        return tripRepository.findByPassengersUser(passenger);
    }

    public List<Trip> getTripsAsDriver(User user) {
        return tripRepository.findByDriver(user);
    }

    // Връща всички пътувания, в които потребителят участва като пътник
    public List<Trip> getTripsAsPassenger(User user) {
        return tripRepository.findByPassengersUser(user);
    }

    // Брой пътувания на потребителя (като шофьор или пътник)
    public int getTripsByUser(User user) {
        int driverTrips = getTripsAsDriver(user).size();
        int passengerTrips = getTripsAsPassenger(user).size();
        return driverTrips + passengerTrips;
    }



    // Намиране на пътуване по ID
    public Optional<Trip> findById(Long tripId) {
        return tripRepository.findById(tripId);
    }

    // Изтриване на пътуване
    public void deleteTrip(Trip trip) {
        tripRepository.delete(trip);
    }


    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public Trip getTripById(Long id) {
        return tripRepository.findById(id).orElse(null);
    }
}