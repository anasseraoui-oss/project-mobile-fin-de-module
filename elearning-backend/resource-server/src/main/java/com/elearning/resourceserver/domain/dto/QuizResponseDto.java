package com.elearning.resourceserver.domain.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class QuizResponseDto {
    private UUID id;
    private UUID courseId;
    private Integer passScore;
    private Integer maxAttempts;
    private Integer timerSeconds;
    private List<QuestionResponseDto> questions;
    private Integer remainingAttempts; // Calculé côté backend

    @Data
    public static class QuestionResponseDto {
        private UUID id;
        private String question;
        private String type;
        private String options; // JSON String
        private String correctAnswer; // Inclus ou non selon le rôle de l'utilisateur (formateur ou apprenant)
        private Integer points;
        private Integer orderIndex;
    }
}
