package com.trippzo.service;

import com.trippzo.model.Notification;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createSeatRequestNotification(Trip trip, User sender) {
        Notification notification = new Notification();
        notification.setRecipient(trip.getDriver());
        notification.setSender(sender);
        notification.setTrip(trip);
        notification.setType("SEAT_REQUEST");
        notification.setMessage(sender.getFullName() + " иска да заяви място на маршрута " + trip.getOrigin() + " → "
                + trip.getDestination());
        notification.setStatus("PENDING");
        return notificationRepository.save(notification);
    }

    public Notification acceptSeatRequest(Long notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.setStatus("ACCEPTED");
            notification.setRespondedAt(LocalDateTime.now());

            createResponseNotification(notification, "SEAT_ACCEPTED", "Вашето место е потвърдено!");

            return notificationRepository.save(notification);
        }
        return null;
    }

    public Notification rejectSeatRequest(Long notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.setStatus("REJECTED");
            notification.setRespondedAt(LocalDateTime.now());

            createResponseNotification(notification, "SEAT_REJECTED", "Вашата заявка е отхвърлена.");

            return notificationRepository.save(notification);
        }
        return null;
    }

    private void createResponseNotification(Notification originalRequest, String type, String message) {
        Notification responseNotification = new Notification();
        responseNotification.setRecipient(originalRequest.getSender());
        responseNotification.setSender(originalRequest.getRecipient());
        responseNotification.setTrip(originalRequest.getTrip());
        responseNotification.setType(type);
        responseNotification.setMessage(message);
        responseNotification.setStatus("PENDING");
        notificationRepository.save(responseNotification);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByRecipientIdAndStatusOrderByCreatedAtDesc(userId, "PENDING");
    }

    public List<Notification> getAllNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public int getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndStatus(userId, "PENDING");
    }

    public Notification markAsRead(Long notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            if ("PENDING".equals(notification.getStatus())) {
                notification.setStatus("READ");
                return notificationRepository.save(notification);
            }
        }
        return null;
    }

    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }

    public Optional<Notification> findSeatRequestNotification(Long tripId, Long userId) {
        return notificationRepository.findByTripIdAndSenderIdAndType(tripId, userId, "SEAT_REQUEST");
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    public void deleteAllNotifications(Long userId) {
        List<Notification> notifications = getAllNotifications(userId);
        notificationRepository.deleteAll(notifications);
    }

    public int countUnread(Long userId) {
        return notificationRepository.countByRecipient_IdAndStatusNot(userId, "READ");
    }
}
