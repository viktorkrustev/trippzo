package com.trippzo.repository;

import com.trippzo.model.Message;
import com.trippzo.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface MessageRepository extends CrudRepository<Message, Long> {

    List<Message> findByTripIdOrderByTimestampAsc(Long tripId);

    // Вземи всички receivers, на които е писал дадения потребител
    @Query("SELECT DISTINCT m.receiver FROM Message m WHERE m.sender.username = :username")
    Set<User> findReceiversBySender(String username);

    // Вземи всички senders, които са писали на дадения потребител
    @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.receiver.username = :username")
    Set<User> findSendersByReceiver(String username);

    @Query("""
            SELECT m FROM Message m
            WHERE (m.sender.username = :userA AND m.receiver.username = :userB)
               OR (m.sender.username = :userB AND m.receiver.username = :userA)
            ORDER BY m.timestamp
            """)
    List<Message> findChatBetweenUsers(String userA, String userB);

    int countBySenderUsernameAndReceiverUsernameAndReadFalse(String senderUsername, String receiverUsername);
    List<Message> findBySenderUsernameAndReceiverUsernameAndReadFalse(String senderUsername, String receiverUsername);

}
