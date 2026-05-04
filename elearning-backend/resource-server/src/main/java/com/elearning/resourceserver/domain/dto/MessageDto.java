package com.elearning.resourceserver.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageDto {
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private LocalDateTime sentAt;
}
