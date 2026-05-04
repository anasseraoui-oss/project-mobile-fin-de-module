package com.elearning.resourceserver.api;

import com.elearning.resourceserver.application.services.ChatAndForumService;
import com.elearning.resourceserver.domain.dto.ForumPostDto;
import com.elearning.resourceserver.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChatAndForumController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatAndForumService chatAndForumService;

    @MessageMapping("/chat.send")
    public void sendDirectMessage(@Payload Map<String, Object> payload) {
        String receiverIdStr = (String) payload.get("receiverId");
        String content = (String) payload.get("content");
        UUID senderId = SecurityUtils.getCurrentUserId(); 
        UUID receiverId = UUID.fromString(receiverIdStr);

        chatAndForumService.saveDirectMessage(senderId, receiverId, content);
        
        messagingTemplate.convertAndSend("/topic/user." + receiverId, Map.of(
                "senderId", senderId.toString(),
                "content", content,
                "sentAt", LocalDateTime.now()
        ));
    }

    @MessageMapping("/forum.post")
    public void postToForum(@Payload Map<String, Object> payload) {
        String seanceIdStr = (String) payload.get("seanceId");
        String content = (String) payload.get("content");
        String parentIdStr = (String) payload.get("parentId"); 
        
        UUID authorId = SecurityUtils.getCurrentUserId();
        UUID seanceId = UUID.fromString(seanceIdStr);
        UUID parentId = parentIdStr != null ? UUID.fromString(parentIdStr) : null;

        chatAndForumService.saveForumPost(authorId, seanceId, content, parentId);

        messagingTemplate.convertAndSend("/topic/seance." + seanceId, Map.of(
                "id", UUID.randomUUID().toString(),
                "authorId", authorId.toString(),
                "content", content,
                "parentId", parentIdStr != null ? parentIdStr : null,
                "createdAt", LocalDateTime.now()
        ));
    }

    @GetMapping("/api/v1/seances/{id}/forum")
    public ResponseEntity<List<ForumPostDto>> getForumBySeance(@PathVariable UUID id) {
        return ResponseEntity.ok(chatAndForumService.getForumBySeance(id));
    }
}
