package com.trippzo.service;

import com.trippzo.model.Notification;
import com.trippzo.model.Trip;
import com.trippzo.model.TripPassenger;
import com.trippzo.model.User;
import com.trippzo.model.enums.NotificationStatus;
import com.trippzo.repository.TripPassengerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тестове за BookingService
 * 
 * Демонстрира как BookingService прави бизнес логиката тестваема
 */
@DisplayName("BookingService Tests")
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
        MockitoAnnotations.openMocks(this);

        // Подготовка на тестови данни
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

        notification = new Notification();
        notification.setId(1L);
        notification.setTrip(trip);
        notification.setSender(passenger);
        notification.setRecipient(driver);
        notification.setStatus(NotificationStatus.PENDING);
    }

    // ================== УСПЕШНИ СЦЕНАРИИ ==================

    @Test
    @DisplayName("acceptSeatRequest успешно когато има свободни места")
    void testAcceptSeatRequest_WithAvailableSeats_Success() {
        // Arrange
        long notificationId = 1L;
        
        when(notificationService.findById(notificationId))
            .thenReturn(Optional.of(notification));
        
        when(tripPassengerRepository.findByTripIdAndUserId(trip.getId(), passenger.getId()))
            .thenReturn(Optional.empty()); // Потребителят не е пътник
        
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(2); // 2 от 4 места са заемки

        // Act
        boolean result = bookingService.acceptSeatRequest(notificationId);

        // Assert
        assertTrue(result, "acceptSeatRequest трябва да върне true");
        verify(tripPassengerRepository).save(any(TripPassenger.class));
        verify(notificationService).acceptSeatRequest(notificationId);
    }

    @Test
    @DisplayName("Проверка наличие на свободни места")
    void testHasAvailableSeats_WhenSeatsAvailable() {
        // Arrange
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(2); // 2 места заемки, 2 свободни

        // Act
        boolean result = bookingService.hasAvailableSeats(trip);

        // Assert
        assertTrue(result, "Трябва да има свободни места");
    }

    @Test
    @DisplayName("Получаване на брой свободни места")
    void testGetAvailableSeats_ReturnsCorrectCount() {
        // Arrange
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(2); // 2 места заемки

        // Act
        int availableSeats = bookingService.getAvailableSeats(trip);

        // Assert
        assertEquals(2, availableSeats, "Трябва 2 свободни места (4 - 2)");
    }

    @Test
    @DisplayName("Проверка дали потребител е пътник - положен случай")
    void testIsUserAlreadyPassenger_WhenUserIsPassenger() {
        // Arrange
        TripPassenger tripPassenger = new TripPassenger();
        tripPassenger.setId(1L);
        tripPassenger.setUser(passenger);
        tripPassenger.setTrip(trip);

        when(tripPassengerRepository.findByTripIdAndUserId(trip.getId(), passenger.getId()))
            .thenReturn(Optional.of(tripPassenger));

        // Act
        boolean result = bookingService.isUserAlreadyPassenger(trip.getId(), passenger.getId());

        // Assert
        assertTrue(result, "Потребителят трябва да е пътник");
    }

    @Test
    @DisplayName("Проверка дали потребител е пътник - отрицателен случай")
    void testIsUserAlreadyPassenger_WhenUserIsNotPassenger() {
        // Arrange
        when(tripPassengerRepository.findByTripIdAndUserId(trip.getId(), passenger.getId()))
            .thenReturn(Optional.empty());

        // Act
        boolean result = bookingService.isUserAlreadyPassenger(trip.getId(), passenger.getId());

        // Assert
        assertFalse(result, "Потребителят не трябва да е пътник");
    }

    // ================== НЕУСПЕШНИ СЦЕНАРИИ ==================

    @Test
    @DisplayName("acceptSeatRequest неуспешно когато няма свободни места")
    void testAcceptSeatRequest_WithNoAvailableSeats_Failure() {
        // Arrange
        long notificationId = 1L;
        
        when(notificationService.findById(notificationId))
            .thenReturn(Optional.of(notification));
        
        when(tripPassengerRepository.findByTripIdAndUserId(trip.getId(), passenger.getId()))
            .thenReturn(Optional.empty());
        
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(4); // Всички 4 места са заемки

        // Act
        boolean result = bookingService.acceptSeatRequest(notificationId);

        // Assert
        assertFalse(result, "acceptSeatRequest трябва да върне false");
        verify(tripPassengerRepository, never()).save(any(TripPassenger.class));
        verify(notificationService).rejectSeatRequest(notificationId);
    }

    @Test
    @DisplayName("acceptSeatRequest неуспешно когато потребител е вече пътник")
    void testAcceptSeatRequest_WhenUserAlreadyPassenger_Failure() {
        // Arrange
        long notificationId = 1L;
        TripPassenger existingPassenger = new TripPassenger();
        existingPassenger.setUser(passenger);
        existingPassenger.setTrip(trip);

        when(notificationService.findById(notificationId))
            .thenReturn(Optional.of(notification));
        
        when(tripPassengerRepository.findByTripIdAndUserId(trip.getId(), passenger.getId()))
            .thenReturn(Optional.of(existingPassenger)); // Потребителят е пътник

        // Act
        boolean result = bookingService.acceptSeatRequest(notificationId);

        // Assert
        assertFalse(result, "acceptSeatRequest трябва да върне false");
        verify(tripPassengerRepository, never()).save(any(TripPassenger.class));
        verify(notificationService).deleteNotification(notificationId);
    }

    @Test
    @DisplayName("acceptSeatRequest неуспешно когато известието не съществува")
    void testAcceptSeatRequest_WhenNotificationNotFound_Failure() {
        // Arrange
        long notificationId = 999L;
        
        when(notificationService.findById(notificationId))
            .thenReturn(Optional.empty());

        // Act
        boolean result = bookingService.acceptSeatRequest(notificationId);

        // Assert
        assertFalse(result, "acceptSeatRequest трябва да върне false");
        verify(tripPassengerRepository, never()).save(any(TripPassenger.class));
    }

    @Test
    @DisplayName("Всички места са заети - няма свободни")
    void testHasAvailableSeats_WhenAllSeatsBooked() {
        // Arrange
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(4); // Всички 4 места са заемки

        // Act
        boolean result = bookingService.hasAvailableSeats(trip);

        // Assert
        assertFalse(result, "Не трябва да има свободни места");
    }

    @Test
    @DisplayName("Всички места са свободни")
    void testGetAvailableSeats_WhenAllSeatsEmpty() {
        // Arrange
        when(tripPassengerRepository.countByTripId(trip.getId()))
            .thenReturn(0); // Никое място не е заемено

        // Act
        int availableSeats = bookingService.getAvailableSeats(trip);

        // Assert
        assertEquals(4, availableSeats, "Всички 4 места трябва да са свободни");
    }

    // ================== РЕДЖЕКТ СЦЕНАРИИ ==================

    @Test
    @DisplayName("rejectSeatRequest делегира към NotificationService")
    void testRejectSeatRequest_DelegatesCorrectly() {
        // Arrange
        long notificationId = 1L;

        // Act
        bookingService.rejectSeatRequest(notificationId);

        // Assert
        verify(notificationService).rejectSeatRequest(notificationId);
    }
}

