package com.trippzo.service;

import com.trippzo.model.Notification;
import com.trippzo.model.Trip;
import com.trippzo.model.TripPassenger;
import com.trippzo.model.User;
import com.trippzo.model.enums.NotificationStatus;
import com.trippzo.repository.TripPassengerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    private TripPassengerRepository tripPassengerRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingService bookingService;

    private Trip trip;
    private User passenger;
    private Notification notification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User driver = new User();
        driver.setId(1L);
        driver.setUsername("driver");

        passenger = new User();
        passenger.setId(2L);
        passenger.setUsername("passenger");

        trip = new Trip();
        trip.setId(1L);
        trip.setSeatsTotal(4);
        trip.setOrigin("Sofia");
        trip.setDestination("Plovdiv");
        trip.setDriver(driver);

        notification = new Notification();
        notification.setId(1L);
        notification.setTrip(trip);
        notification.setSender(passenger);
        notification.setRecipient(driver);
        notification.setStatus(NotificationStatus.PENDING);
    }

    @Test
    void testAcceptSeatRequest_WithAvailableSeats_Success() {
        long notificationId = 1L;

        when(notificationService.findById(notificationId)).thenReturn(Optional.of(notification));

        when(tripPassengerRepository.findByTripIdAndUserId(trip.getId(), passenger.getId()))
                .thenReturn(Optional.empty());

        when(tripPassengerRepository.countByTripId(trip.getId())).thenReturn(2);

        boolean result = bookingService.acceptSeatRequest(notificationId);

        assertTrue(result, "acceptSeatRequest трябва да върне true");
        verify(tripPassengerRepository).save(any(TripPassenger.class));
        verify(notificationService).acceptSeatRequest(notificationId);
    }

    @Test
    void testHasAvailableSeats_WhenSeatsAvailable() {
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(2);
        boolean result = bookingService.hasAvailableSeats(trip);

        assertTrue(result, "Трябва да има свободни места");
    }

    @Test
    void testGetAvailableSeats_ReturnsCorrectCount() {
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(2);

        int availableSeats = bookingService.getAvailableSeats(trip);

        assertEquals(2, availableSeats, "Трябва 2 свободни места (4 - 2)");
    }

    @Test
    void testIsUserAlreadyPassenger_WhenUserIsPassenger() {
        TripPassenger tripPassenger = new TripPassenger();
        tripPassenger.setId(1L);
        tripPassenger.setUser(passenger);
        tripPassenger.setTrip(trip);

        when(tripPassengerRepository.findByTripIdAndUserId(trip.getId(), passenger.getId()))
                .thenReturn(Optional.of(tripPassenger));

        boolean result = bookingService.isUserAlreadyPassenger(trip.getId(), passenger.getId());

        assertTrue(result, "Потребителят трябва да е пътник");
    }

    @Test
    void testIsUserAlreadyPassenger_WhenUserIsNotPassenger() {
        when(tripPassengerRepository.findByTripIdAndUserId(trip.getId(), passenger.getId()))
            .thenReturn(Optional.empty());

        boolean result = bookingService.isUserAlreadyPassenger(trip.getId(), passenger.getId());

        assertFalse(result, "Потребителят не трябва да е пътник");
    }

    @Test
    void testAcceptSeatRequest_WithNoAvailableSeats_Failure() {
        long notificationId = 1L;

        when(notificationService.findById(notificationId)).thenReturn(Optional.of(notification));

        when(tripPassengerRepository.findByTripIdAndUserId(trip.getId(), passenger.getId()))
                .thenReturn(Optional.empty());

        when(tripPassengerRepository.countByTripId(trip.getId())).thenReturn(4);
        boolean result = bookingService.acceptSeatRequest(notificationId);

        assertFalse(result, "acceptSeatRequest трябва да върне false");
        verify(tripPassengerRepository, never()).save(any(TripPassenger.class));
        verify(notificationService).rejectSeatRequest(notificationId);
    }

    @Test
    void testAcceptSeatRequest_WhenUserAlreadyPassenger_Failure() {
        long notificationId = 1L;
        TripPassenger existingPassenger = new TripPassenger();
        existingPassenger.setUser(passenger);
        existingPassenger.setTrip(trip);

        when(notificationService.findById(notificationId)).thenReturn(Optional.of(notification));

        when(tripPassengerRepository.findByTripIdAndUserId(trip.getId(), passenger.getId()))
                .thenReturn(Optional.of(existingPassenger));

        boolean result = bookingService.acceptSeatRequest(notificationId);

        assertFalse(result, "acceptSeatRequest трябва да върне false");
        verify(tripPassengerRepository, never()).save(any(TripPassenger.class));
        verify(notificationService).deleteNotification(notificationId);
    }

    @Test
    void testAcceptSeatRequest_WhenNotificationNotFound_Failure() {
        long notificationId = 999L;

        when(notificationService.findById(notificationId)).thenReturn(Optional.empty());

        boolean result = bookingService.acceptSeatRequest(notificationId);

        assertFalse(result, "acceptSeatRequest трябва да върне false");
        verify(tripPassengerRepository, never()).save(any(TripPassenger.class));
    }

    @Test
    void testHasAvailableSeats_WhenAllSeatsBooked() {
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(4);

        boolean result = bookingService.hasAvailableSeats(trip);

        assertFalse(result, "Не трябва да има свободни места");
    }

    @Test
    void testGetAvailableSeats_WhenAllSeatsEmpty() {
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(0);

        int availableSeats = bookingService.getAvailableSeats(trip);

        assertEquals(4, availableSeats, "Всички 4 места трябва да са свободни");
    }

    @Test
    void testRejectSeatRequest_DelegatesCorrectly() {
        long notificationId = 1L;

        bookingService.rejectSeatRequest(notificationId);

        verify(notificationService).rejectSeatRequest(notificationId);
    }
}
