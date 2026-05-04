package com.elearning.resourceserver.domain.dto;

import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
public class QuizResultDto {
    private Integer score;
    private Boolean passed;
    private Map<String, String> corrections; // Map <QuestionId (UUID String), CorrectAnswer JSON>
    private UUID attemptId;
    private Integer remainingAttempts;
}
