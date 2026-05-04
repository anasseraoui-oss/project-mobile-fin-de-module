package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.ForumPost;
import com.elearning.resourceserver.domain.Message;
import com.elearning.resourceserver.domain.dto.ForumPostDto;
import com.elearning.resourceserver.repository.ForumPostRepository;
import com.elearning.resourceserver.repository.MessageRepository;
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

    public Message saveDirectMessage(UUID senderId, UUID receiverId, String content) {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setSentAt(LocalDateTime.now());
        msg.setRead(false);
        return messageRepository.save(msg);
    }

    public ForumPost saveForumPost(UUID authorId, UUID seanceId, String content, UUID parentId) {
        ForumPost post = new ForumPost();
        post.setAuthorId(authorId);
        post.setSeanceId(seanceId);
        post.setContent(content);
        post.setParentId(parentId);
        post.setCreatedAt(LocalDateTime.now());
        return forumPostRepository.save(post);
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
        dto.setAuthorId(post.getAuthorId());
        dto.setSeanceId(post.getSeanceId());
        dto.setContent(post.getContent());
        dto.setParentId(post.getParentId());
        dto.setCreatedAt(post.getCreatedAt());
        return dto;
    }
}
