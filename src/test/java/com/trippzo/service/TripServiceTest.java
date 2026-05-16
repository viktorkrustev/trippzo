package com.trippzo.service;

import com.trippzo.model.Trip;
import com.trippzo.model.TripPassenger;
import com.trippzo.model.User;
import com.trippzo.model.enums.Role;
import com.trippzo.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private TripService tripService;

    private Trip testTrip;
    private User testDriver;
    private User testPassenger;

    @BeforeEach
    void setUp() {
        testDriver = new User();
        testDriver.setId(1L);
        testDriver.setUsername("driver");
        testDriver.setEmail("driver@example.com");
        testDriver.setRole(Role.ROLE_USER);

        testPassenger = new User();
        testPassenger.setId(2L);
        testPassenger.setUsername("passenger");
        testPassenger.setEmail("passenger@example.com");

        testTrip = new Trip();
        testTrip.setId(1L);
        testTrip.setOrigin("Sofia");
        testTrip.setDestination("Plovdiv");
        testTrip.setSeatsTotal(4);
        testTrip.setPricePerSeat(BigDecimal.valueOf(15.00));
        testTrip.setDriver(testDriver);
        testTrip.setActive(true);
        testTrip.setDepartureDateTime(LocalDateTime.now().plusDays(1));
        testTrip.setPassengers(new ArrayList<>());
    }

    @Test
    void testFindById() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        Optional<Trip> result = tripService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Sofia", result.get().getOrigin());
        assertEquals("Plovdiv", result.get().getDestination());
    }

    @Test
    void testFindByIdNotFound() {
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Trip> result = tripService.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetTripById() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        Trip result = tripService.getTripById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetTripByIdNotFound() {
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        Trip result = tripService.getTripById(999L);

        assertNull(result);
    }

    @Test
    void testGetTripsAsDriver() {
        List<Trip> trips = List.of(testTrip);
        when(tripRepository.findByDriver(testDriver)).thenReturn(trips);

        List<Trip> result = tripService.getTripsAsDriver(testDriver);

        assertEquals(1, result.size());
        assertEquals("Sofia", result.getFirst().getOrigin());
    }

    @Test
    void testGetTripsAsPassenger() {
        List<Trip> trips = List.of(testTrip);
        when(tripRepository.findByPassengersUser(testPassenger)).thenReturn(trips);

        List<Trip> result = tripService.getTripsAsPassenger(testPassenger);

        assertEquals(1, result.size());
    }

    @Test
    void testGetTripsByUser() {
        when(tripRepository.countByDriver(testDriver)).thenReturn(2);
        when(tripRepository.countByPassengersUser(testDriver)).thenReturn(3);

        int result = tripService.getTripsByUser(testDriver);

        assertEquals(5, result);
    }

    @Test
    void testDeleteTrip() {
        tripService.deleteTrip(testTrip);

        verify(tripRepository, times(1)).delete(testTrip);
    }

    @Test
    void testGetAvailableSeatsWithPassengers() {
        TripPassenger passenger1 = new TripPassenger();
        passenger1.setUser(testPassenger);

        testTrip.setPassengers(new ArrayList<>(List.of(passenger1)));

        int availableSeats = tripService.getAvailableSeats(testTrip);

        assertEquals(3, availableSeats);
    }

    @Test
    void testGetAvailableSeatsNoPassengers() {
        int availableSeats = tripService.getAvailableSeats(testTrip);

        assertEquals(4, availableSeats);
    }

    @Test
    void testGetAvailableSeatsAllFull() {
        List<TripPassenger> passengers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            TripPassenger p = new TripPassenger();
            p.setUser(new User());
            passengers.add(p);
        }
        testTrip.setPassengers(passengers);

        int availableSeats = tripService.getAvailableSeats(testTrip);

        assertEquals(0, availableSeats);
    }

    @Test
    void testGetAvailableSeatsNullPassengers() {
        testTrip.setPassengers(null);

        int availableSeats = tripService.getAvailableSeats(testTrip);

        assertEquals(0, availableSeats);
    }

    @Test
    void testGetAvailableSeatsNullTrip() {
        int availableSeats = tripService.getAvailableSeats(null);

        assertEquals(0, availableSeats);
    }

    @Test
    void testIsUserDriver() {
        boolean isDriver = tripService.isUserDriver(testTrip, testDriver);

        assertTrue(isDriver);
    }

    @Test
    void testIsUserNotDriver() {
        boolean isDriver = tripService.isUserDriver(testTrip, testPassenger);

        assertFalse(isDriver);
    }

    @Test
    void testIsUserDriverNullUser() {
        boolean isDriver = tripService.isUserDriver(testTrip, null);

        assertFalse(isDriver);
    }

    @Test
    void testIsUserDriverNullTrip() {
        boolean isDriver = tripService.isUserDriver(null, testDriver);

        assertFalse(isDriver);
    }

    @Test
    void testIsUserDriverNullDriver() {
        testTrip.setDriver(null);

        boolean isDriver = tripService.isUserDriver(testTrip, testDriver);

        assertFalse(isDriver);
    }
}
