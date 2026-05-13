package com.trippzo.service;

import com.trippzo.model.Notification;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.model.enums.NotificationStatus;
import com.trippzo.model.enums.NotificationType;
import com.trippzo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createSeatRequestNotification(Trip trip, User sender) {
        Notification notification = new Notification();
        notification.setRecipient(trip.getDriver());
        notification.setSender(sender);
        notification.setTrip(trip);
        notification.setType(NotificationType.SEAT_REQUEST);
        notification.setMessage(String.format("%s иска да заяви място за маршрута %s → %s", sender.getFullName(),
                trip.getOrigin(), trip.getDestination()));
        notification.setStatus(NotificationStatus.PENDING);
        notificationRepository.save(notification);
    }

    @Transactional
    public void acceptSeatRequest(Long notificationId) {
        notificationRepository.findById(notificationId).map(notification -> {
            notification.setStatus(NotificationStatus.ACCEPTED);
            notification.setRespondedAt(LocalDateTime.now());
            createResponseNotification(notification, NotificationType.SEAT_ACCEPTED, "Вашето място е потвърдено!");
            return notificationRepository.save(notification);
        });
    }

    @Transactional
    public void rejectSeatRequest(Long notificationId) {
        notificationRepository.findById(notificationId).map(notification -> {
            notification.setStatus(NotificationStatus.REJECTED);
            notification.setRespondedAt(LocalDateTime.now());
            createResponseNotification(notification, NotificationType.SEAT_REJECTED, "Вашата заявка беше отхвърлена.");
            return notificationRepository.save(notification);
        });
    }

    private void createResponseNotification(Notification original, NotificationType type, String message) {
        Notification response = new Notification();
        response.setRecipient(original.getSender());
        response.setSender(original.getRecipient());
        response.setTrip(original.getTrip());
        response.setType(type);
        response.setMessage(message);
        response.setStatus(NotificationStatus.PENDING);
        notificationRepository.save(response);
    }

    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Notification> findSeatRequestNotification(Long tripId, Long userId) {
        return notificationRepository.findByTripIdAndSenderIdAndType(tripId, userId, NotificationType.SEAT_REQUEST);
    }

    @Transactional(readOnly = true)
    public boolean hasExistingSeatRequest(Long tripId, Long userId) {
        return findSeatRequestNotification(tripId, userId).isPresent();
    }

    @Transactional(readOnly = true)
    public int countUnread(Long userId) {
        return notificationRepository.countByRecipientIdAndStatusNot(userId, NotificationStatus.READ);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getStatus() != NotificationStatus.READ) {
                n.setStatus(NotificationStatus.READ);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Transactional
    public void deleteAllNotifications(Long userId) {
        notificationRepository.deleteByRecipientId(userId);
    }
}
