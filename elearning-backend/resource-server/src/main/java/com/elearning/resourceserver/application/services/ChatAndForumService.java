package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.ForumPost;
import com.elearning.resourceserver.domain.Message;
import com.elearning.resourceserver.domain.dto.ForumPostDto;
import com.elearning.resourceserver.domain.dto.MessageDto;
import com.elearning.resourceserver.repository.ForumPostRepository;
import com.elearning.resourceserver.repository.MessageRepository;
import com.elearning.resourceserver.repository.UserRepository;
import com.elearning.resourceserver.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatAndForumService {

    private final MessageRepository messageRepository;
    private final ForumPostRepository forumPostRepository;
    private final UserRepository userRepository;
    private final SeanceRepository seanceRepository;

    public Message saveDirectMessage(UUID senderId, UUID receiverId, String content) {
        Message msg = new Message();
        msg.setSender(userRepository.getReferenceById(senderId));
        msg.setReceiver(userRepository.getReferenceById(receiverId));
        msg.setContent(content);
        msg.setSentAt(LocalDateTime.now());
        msg.setIsRead(false);
        return messageRepository.save(msg);
    }

    public MessageDto sendDirectMessage(UUID senderId, UUID receiverId, String content) {
        return mapToDto(saveDirectMessage(senderId, receiverId, content));
    }

    public List<MessageDto> getConversation(UUID currentUserId, UUID otherUserId) {
        return messageRepository
                .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(
                        currentUserId, otherUserId, otherUserId, currentUserId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ForumPost saveForumPost(UUID authorId, UUID seanceId, String content, UUID parentId) {
        ForumPost post = new ForumPost();
        post.setAuthor(userRepository.getReferenceById(authorId));
        post.setSeance(seanceRepository.getReferenceById(seanceId));
        post.setContent(content);
        if (parentId != null) {
            post.setParent(forumPostRepository.getReferenceById(parentId));
        }
        post.setCreatedAt(LocalDateTime.now());
        return forumPostRepository.save(post);
    }

    public ForumPostDto createForumPost(UUID authorId, UUID seanceId, String content, UUID parentId) {
        return mapToDto(saveForumPost(authorId, seanceId, content, parentId));
    }

    public List<ForumPostDto> getForumBySeance(UUID seanceId) {
        return forumPostRepository.findBySeanceIdOrderByCreatedAt(seanceId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ForumPostDto mapToDto(ForumPost post) {
        ForumPostDto dto = new ForumPostDto();
        dto.setId(post.getId());
        dto.setAuthorId(post.getAuthor() != null ? post.getAuthor().getId() : null);
        dto.setSeanceId(post.getSeance() != null ? post.getSeance().getId() : null);
        dto.setContent(post.getContent());
        dto.setParentId(post.getParent() != null ? post.getParent().getId() : null);
        dto.setCreatedAt(post.getCreatedAt());
        return dto;
    }

    private MessageDto mapToDto(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender() != null ? message.getSender().getId() : null);
        dto.setReceiverId(message.getReceiver() != null ? message.getReceiver().getId() : null);
        dto.setContent(message.getContent());
        dto.setIsRead(message.getIsRead());
        dto.setSentAt(message.getSentAt());
        return dto;
    }
}
