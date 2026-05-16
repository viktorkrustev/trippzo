package com.trippzo.service;

import com.trippzo.model.Notification;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.model.enums.NotificationStatus;
import com.trippzo.model.enums.NotificationType;
import com.trippzo.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Trip testTrip;
    private User passenger;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        User driver = new User();
        driver.setId(1L);
        driver.setUsername("driver");
        driver.setFullName("Driver User");

        passenger = new User();
        passenger.setId(2L);
        passenger.setUsername("passenger");
        passenger.setFullName("Passenger User");

        testTrip = new Trip();
        testTrip.setId(1L);
        testTrip.setOrigin("Sofia");
        testTrip.setDestination("Plovdiv");
        testTrip.setSeatsTotal(4);
        testTrip.setPricePerSeat(BigDecimal.valueOf(15.00));
        testTrip.setDriver(driver);
        testTrip.setDepartureDateTime(LocalDateTime.now().plusDays(1));

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setRecipient(driver);
        testNotification.setSender(passenger);
        testNotification.setTrip(testTrip);
        testNotification.setType(NotificationType.SEAT_REQUEST);
        testNotification.setStatus(NotificationStatus.PENDING);
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateSeatRequestNotification() {
        notificationService.createSeatRequestNotification(testTrip, passenger);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testAcceptSeatRequest() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.acceptSeatRequest(1L);

        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, atLeast(1)).save(any(Notification.class));
    }

    @Test
    void testAcceptSeatRequestNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        notificationService.acceptSeatRequest(999L);

        verify(notificationRepository, times(1)).findById(999L);
    }

    @Test
    void testRejectSeatRequest() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.rejectSeatRequest(1L);

        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, atLeast(1)).save(any(Notification.class));
    }

    @Test
    void testRejectSeatRequestNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        notificationService.rejectSeatRequest(999L);

        verify(notificationRepository, times(1)).findById(999L);
    }

    @Test
    void testGetAllNotifications() {
        List<Notification> notifications = List.of(testNotification);
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L)).thenReturn(notifications);

        List<Notification> result = notificationService.getAllNotifications(1L);

        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).findByRecipientIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void testGetAllNotificationsEmpty() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        List<Notification> result = notificationService.getAllNotifications(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindById() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        Optional<Notification> result = notificationService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(NotificationType.SEAT_REQUEST, result.get().getType());
    }

    @Test
    void testFindByIdNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Notification> result = notificationService.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindSeatRequestNotification() {
        when(notificationRepository.findByTripIdAndSenderIdAndType(1L, 2L, NotificationType.SEAT_REQUEST))
            .thenReturn(Optional.of(testNotification));

        Optional<Notification> result = notificationService.findSeatRequestNotification(1L, 2L);

        assertTrue(result.isPresent());
    }

    @Test
    void testFindSeatRequestNotificationNotFound() {
        when(notificationRepository.findByTripIdAndSenderIdAndType(1L, 2L, NotificationType.SEAT_REQUEST))
            .thenReturn(Optional.empty());

        Optional<Notification> result = notificationService.findSeatRequestNotification(1L, 2L);

        assertFalse(result.isPresent());
    }

    @Test
    void testHasExistingSeatRequest() {
        when(notificationRepository.findByTripIdAndSenderIdAndType(1L, 2L, NotificationType.SEAT_REQUEST))
            .thenReturn(Optional.of(testNotification));

        boolean result = notificationService.hasExistingSeatRequest(1L, 2L);

        assertTrue(result);
    }

    @Test
    void testHasNoExistingSeatRequest() {
        when(notificationRepository.findByTripIdAndSenderIdAndType(1L, 2L, NotificationType.SEAT_REQUEST))
            .thenReturn(Optional.empty());

        boolean result = notificationService.hasExistingSeatRequest(1L, 2L);

        assertFalse(result);
    }

    @Test
    void testCountUnread() {
        when(notificationRepository.countByRecipientIdAndStatusNot(1L, NotificationStatus.READ))
            .thenReturn(3);

        int count = notificationService.countUnread(1L);

        assertEquals(3, count);
    }

    @Test
    void testCountUnreadZero() {
        when(notificationRepository.countByRecipientIdAndStatusNot(1L, NotificationStatus.READ))
            .thenReturn(0);

        int count = notificationService.countUnread(1L);

        assertEquals(0, count);
    }

    @Test
    void testMarkAsRead() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.markAsRead(1L);

        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).save(testNotification);
    }

    @Test
    void testMarkAsReadNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        notificationService.markAsRead(999L);

        verify(notificationRepository, times(1)).findById(999L);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkAsReadAlreadyRead() {
        testNotification.setStatus(NotificationStatus.READ);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        notificationService.markAsRead(1L);

        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testDeleteNotification() {
        notificationService.deleteNotification(1L);

        verify(notificationRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteAllNotifications() {
        notificationService.deleteAllNotifications(1L);

        verify(notificationRepository, times(1)).deleteByRecipientId(1L);
    }
}
