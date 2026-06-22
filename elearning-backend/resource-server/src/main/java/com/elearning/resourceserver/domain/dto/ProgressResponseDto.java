package com.elearning.resourceserver.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProgressResponseDto {
    private UUID seanceId;
    private Integer watchedSeconds;
    private Boolean completed;
    private LocalDateTime lastWatchedAt;
    private Integer courseProgressPercent;
}
