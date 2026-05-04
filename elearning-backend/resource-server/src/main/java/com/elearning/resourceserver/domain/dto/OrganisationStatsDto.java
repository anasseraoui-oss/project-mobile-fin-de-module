package com.elearning.resourceserver.domain.dto;

import lombok.Data;

@Data
public class OrganisationStatsDto {
    private Long totalFormations;
    private Long totalApprenants;
    private Double avgCompletionRate;
    private Double revenus;
}
