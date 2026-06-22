package com.elearning.resourceserver.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class QuizSubmitDto {
    private UUID tentativeId;

    @NotEmpty(message = "Les reponses sont obligatoires")
    private Map<String, String> answers;
}
