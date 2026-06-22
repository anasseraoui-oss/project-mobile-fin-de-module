package com.elearning.resourceserver.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PedagogicalResourceDto {
    private UUID id;
    private UUID formationId;
    private UUID courseId;
    private UUID seanceId;
    private String type;
    private String title;
    private String fileName;
    private String objectKey;
    private String fileKey;
    private String fileUrl;
    private String mimeType;
    private Long sizeBytes;
    private Long fileSize;
    private Boolean isDownloadable;
    private Integer version;
    private LocalDateTime createdAt;
}
