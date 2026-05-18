package com.elearning.resourceserver.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SeanceResponseDto {
    private UUID id;
    private String title;
    private UUID courseId;
    private String type;
    private String videoKey;
    private String pdfKey;
    private Integer durationSeconds;
    private String meetingLink;
    private LocalDateTime scheduledAt;
    private Integer orderIndex;
    private Boolean isPublished;
    
    private String streamUrlExpiry;
    private Boolean isCompleted;
    private String status;
}
