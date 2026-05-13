package com.trippzo.repository;

import com.trippzo.model.Notification;
import com.trippzo.model.enums.NotificationStatus;
import com.trippzo.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);


    Optional<Notification> findByTripIdAndSenderIdAndType(Long tripId, Long senderId, NotificationType type);


    int countByRecipientIdAndStatusNot(Long recipientId, NotificationStatus status);


    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient.id = :recipientId")
    void deleteByRecipientId(@Param("recipientId") Long recipientId);
}