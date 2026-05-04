package com.elearning.resourceserver.domain.dto;

import lombok.Data;

@Data
public class TrainerStatsDto {
    private Long totalCourses;
    private Long totalApprenants;
    private Double avgQuizScore;
}
