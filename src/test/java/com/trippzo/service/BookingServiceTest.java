package com.trippzo.service;

import com.trippzo.model.Notification;
import com.trippzo.model.Trip;
import com.trippzo.model.TripPassenger;
import com.trippzo.model.User;
import com.trippzo.model.enums.NotificationStatus;
import com.trippzo.repository.TripPassengerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private TripPassengerRepository tripPassengerRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingService bookingService;

    private Trip trip;
    private User driver;
    private User passenger;
    private Notification notification;

    @BeforeEach
    void setUp() {
        driver = new User();
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
        trip.setPricePerSeat(BigDecimal.valueOf(15.00));
        trip.setDepartureDateTime(LocalDateTime.now().plusDays(1));

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

        assertTrue(result);
        verify(tripPassengerRepository).save(any(TripPassenger.class));
        verify(notificationService).acceptSeatRequest(notificationId);
    }

    @Test
    void testHasAvailableSeats_WhenSeatsAvailable() {
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(2);
        boolean result = bookingService.hasAvailableSeats(trip);

        assertTrue(result);
    }

    @Test
    void testGetAvailableSeats_ReturnsCorrectCount() {
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(2);

        int availableSeats = bookingService.getAvailableSeats(trip);

        assertEquals(2, availableSeats);
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

        assertTrue(result);
    }

    @Test
    void testIsUserAlreadyPassenger_WhenUserIsNotPassenger() {
        when(tripPassengerRepository.findByTripIdAndUserId(trip.getId(), passenger.getId()))
            .thenReturn(Optional.empty());

        boolean result = bookingService.isUserAlreadyPassenger(trip.getId(), passenger.getId());

        assertFalse(result);
    }

    @Test
    void testAcceptSeatRequest_WithNoAvailableSeats_Failure() {
        long notificationId = 1L;

        when(notificationService.findById(notificationId)).thenReturn(Optional.of(notification));

        when(tripPassengerRepository.findByTripIdAndUserId(trip.getId(), passenger.getId()))
                .thenReturn(Optional.empty());

        when(tripPassengerRepository.countByTripId(trip.getId())).thenReturn(4);
        boolean result = bookingService.acceptSeatRequest(notificationId);

        assertFalse(result);
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

        assertFalse(result);
        verify(tripPassengerRepository, never()).save(any(TripPassenger.class));
        verify(notificationService).deleteNotification(notificationId);
    }

    @Test
    void testAcceptSeatRequest_WhenNotificationNotFound_Failure() {
        long notificationId = 999L;

        when(notificationService.findById(notificationId)).thenReturn(Optional.empty());

        boolean result = bookingService.acceptSeatRequest(notificationId);

        assertFalse(result);
        verify(tripPassengerRepository, never()).save(any(TripPassenger.class));
    }

    @Test
    void testHasAvailableSeats_WhenAllSeatsBooked() {
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(4);

        boolean result = bookingService.hasAvailableSeats(trip);

        assertFalse(result);
    }

    @Test
    void testGetAvailableSeats_WhenAllSeatsEmpty() {
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(0);

        int availableSeats = bookingService.getAvailableSeats(trip);

        assertEquals(4, availableSeats);
    }

    @Test
    void testRejectSeatRequest_DelegatesCorrectly() {
        long notificationId = 1L;

        bookingService.rejectSeatRequest(notificationId);

        verify(notificationService).rejectSeatRequest(notificationId);
    }

    @Test
    void testCanRequestSeat_Success() {
        when(notificationService.hasExistingSeatRequest(1L, 2L)).thenReturn(false);

        boolean result = bookingService.canRequestSeat(trip, passenger);

        assertTrue(result);
    }

    @Test
    void testCanRequestSeat_DriverCannotRequest() {
        boolean result = bookingService.canRequestSeat(trip, driver);

        assertFalse(result);
        verify(notificationService, never()).hasExistingSeatRequest(anyLong(), anyLong());
    }

    @Test
    void testCanRequestSeat_ExistingRequest() {
        when(notificationService.hasExistingSeatRequest(1L, 2L)).thenReturn(true);

        boolean result = bookingService.canRequestSeat(trip, passenger);

        assertFalse(result);
    }

    @Test
    void testCanRequestSeat_NullTrip() {
        assertThrows(IllegalArgumentException.class, () -> bookingService.canRequestSeat(null, passenger));
    }

    @Test
    void testCanRequestSeat_NullPassenger() {
        assertThrows(IllegalArgumentException.class, () -> bookingService.canRequestSeat(trip, null));
    }
}
