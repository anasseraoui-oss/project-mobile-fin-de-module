package com.elearning.resourceserver.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class FormationResponseDto {
    private UUID id;
    private String title;
    private String slug;
    private String description;
    private String thumbnailKey;
    private String thumbnailUrl;
    private String coverImageKey;
    private String coverImageUrl;
    private String previewVideoUrl;
    private String level;
    private String language;
    private Integer totalDuration;
    private Integer durationHours;
    private Double price;
    private Boolean isPublished;
    private String status;
    private List<UUID> prerequisiteIds;
    private String certificateTemplateKey;
    private LocalDateTime createdAt;
    
    private UUID organisationId;
    private String organisationName;
    private UUID formateurId;
    
    private Integer coursesCount;
    private Integer enrolledCount;
    private Float rating;
    private Boolean isEnrolled;
    private Integer progressPercent;
    private String categoryId;
    private List<String> prerequisites;
    private Boolean certified;
}
