package com.trippzo.repository;

import com.trippzo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndStatusOrderByCreatedAtDesc(Long recipientId, String status);

    List<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(Long recipientId, String type);

    int countByRecipientIdAndStatus(Long recipientId, String status);

    List<Notification> findByTripIdAndType(Long tripId, String type);

    Optional<Notification> findByTripIdAndSenderIdAndType(Long tripId, Long senderId, String type);

    int countByRecipient_IdAndStatusNot(Long userId, String status);
}
