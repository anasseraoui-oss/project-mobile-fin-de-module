package com.elearning.resourceserver.domain.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class QuizResponseDto {
    private UUID id;
    private UUID courseId;
    private String title;
    private Integer passScore;
    private Integer maxAttempts;
    private Integer timeLimit;
    private List<QuestionResponseDto> questions;
    private Integer remainingAttempts;

    @Data
    public static class QuestionResponseDto {
        private UUID id;
        private String question;
        private String type;
        private List<Map<String, Object>> reponses;
        private Integer points;
        private Integer orderIndex;
    }
}
