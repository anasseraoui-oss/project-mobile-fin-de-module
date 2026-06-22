package com.elearning.resourceserver.domain.dto;

import lombok.Data;

@Data
public class InstructorStatsDto {
    private Integer activeFormations;
    private Integer totalLearners;
    private Integer averageCompletionPercent;
    private Double monthlyRevenue;
    private String monthlyRevenueCurrency;
    private Integer pendingActions;
}
