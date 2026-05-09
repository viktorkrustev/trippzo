package com.trippzo.service;

import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.model.dto.TripCreateDTO;
import com.trippzo.repository.TripRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class TripService {

    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public Page<Trip> searchTrips(String origin, String destination, String dateString, Pageable pageable) {
        Specification<Trip> spec = TripSpecifications.searchTrips(origin, destination, dateString);
        return tripRepository.findAll(spec, pageable);
    }

    public List<Trip> getTripsAsDriver(User user) {
        return tripRepository.findByDriver(user);
    }

    public List<Trip> getTripsAsPassenger(User user) {
        return tripRepository.findByPassengersUser(user);
    }

    public int getTripsByUser(User user) {
        return getTripsAsDriver(user).size() + getTripsAsPassenger(user).size();
    }

    public Optional<Trip> findById(Long tripId) {
        return tripRepository.findById(tripId);
    }

    public void deleteTrip(Trip trip) {
        tripRepository.delete(trip);
    }

    public Trip getTripById(Long id) {
        return tripRepository.findById(id).orElse(null);
    }

    public void createNewTrip(TripCreateDTO tripDto, User driver) {
        Trip trip = new Trip();
        trip.setOrigin(tripDto.getOrigin());
        trip.setDestination(tripDto.getDestination());
        trip.setCar(tripDto.getCar());
        trip.setSeatsTotal(tripDto.getSeatsTotal());
        trip.setDescription(tripDto.getDescription());

        trip.setPricePerSeat(tripDto.getPricePerSeat());

        String dateTimeStr = tripDto.getDepartureDate() + " " + tripDto.getDepartureTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        trip.setDepartureDateTime(LocalDateTime.parse(dateTimeStr, formatter));

        trip.setDriver(driver);
        trip.setActive(true);

        tripRepository.save(trip);
    }
}
