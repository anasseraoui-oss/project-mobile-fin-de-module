// src/main/java/com/elearning/resourceserver/repository/MessageRepository.java
package com.elearning.resourceserver.repository;

import com.elearning.resourceserver.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(
            UUID senderId,
            UUID receiverId,
            UUID reverseSenderId,
            UUID reverseReceiverId);
}
