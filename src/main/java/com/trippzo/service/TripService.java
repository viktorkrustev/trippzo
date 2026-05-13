package com.trippzo.service;

import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.model.dto.TripCreateDTO;
import com.trippzo.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TripService {

    private static final DateTimeFormatter DEPARTURE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final TripRepository tripRepository;

    public Page<Trip> searchTrips(String origin, String destination, String dateString, Pageable pageable) {
        return tripRepository.findAll(TripSpecifications.searchTrips(origin, destination, dateString), pageable);
    }

    public List<Trip> getTripsAsDriver(User user) {
        return tripRepository.findByDriver(user);
    }

    public List<Trip> getTripsAsPassenger(User user) {
        return tripRepository.findByPassengersUser(user);
    }

    public int getTripsByUser(User user) {
        return tripRepository.countByDriver(user) + tripRepository.countByPassengersUser(user);
    }

    public Optional<Trip> findById(Long tripId) {
        return tripRepository.findById(tripId);
    }

    @Transactional
    public void deleteTrip(Trip trip) {
        tripRepository.delete(trip);
    }

    public Trip getTripById(Long id) {
        return tripRepository.findById(id).orElse(null);
    }

    @Transactional
    public void createNewTrip(TripCreateDTO tripDto, User driver) {
        String dateTimeStr = tripDto.getDepartureDate() + " " + tripDto.getDepartureTime();

        Trip trip = new Trip();
        trip.setOrigin(tripDto.getOrigin());
        trip.setDestination(tripDto.getDestination());
        trip.setCar(tripDto.getCar());
        trip.setSeatsTotal(tripDto.getSeatsTotal());
        trip.setDescription(tripDto.getDescription());
        trip.setPricePerSeat(tripDto.getPricePerSeat());
        trip.setDepartureDateTime(LocalDateTime.parse(dateTimeStr, DEPARTURE_FORMATTER));
        trip.setDriver(driver);
        trip.setActive(true);

        tripRepository.save(trip);
    }

    @Transactional(readOnly = true)
    public int getAvailableSeats(Trip trip) {
        if (trip == null || trip.getPassengers() == null) {
            return 0;
        }
        int passengersCount = trip.getPassengers().size();
        return Math.max(0, trip.getSeatsTotal() - passengersCount);
    }

    @Transactional(readOnly = true)
    public boolean isUserDriver(Trip trip, User user) {
        return user != null && trip != null && trip.getDriver() != null
                && trip.getDriver().getId().equals(user.getId());
    }
}
