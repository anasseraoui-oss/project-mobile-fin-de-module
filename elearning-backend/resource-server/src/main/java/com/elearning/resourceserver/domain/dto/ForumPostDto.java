package com.elearning.resourceserver.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ForumPostDto {
    private UUID id;
    private UUID authorId;
    private UUID seanceId;
    private String content;
    private UUID parentId;
    private LocalDateTime createdAt;
}
