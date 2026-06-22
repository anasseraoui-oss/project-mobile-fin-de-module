package com.elearning.resourceserver.domain.dto;

import com.elearning.resourceserver.domain.enums.TentativeQuizStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class QuizHistoryItemDto {
    private UUID attemptId;
    private UUID quizId;
    private String quizTitle;
    private UUID courseId;
    private String courseTitle;
    private UUID formationId;
    private String formationTitle;
    private LocalDateTime submittedAt;
    private BigDecimal score;
    private TentativeQuizStatus status;
    private Integer attemptNumber;
    private Boolean passed;
    private Boolean certificateAvailable;
}
