package com.elearning.resourceserver.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.Map;

@Data
public class QuizSubmitDto {
    @NotEmpty(message = "Les réponses sont obligatoires")
    private Map<String, String> answers; // Map <QuestionId (UUID String), Reponse JSON>
}
