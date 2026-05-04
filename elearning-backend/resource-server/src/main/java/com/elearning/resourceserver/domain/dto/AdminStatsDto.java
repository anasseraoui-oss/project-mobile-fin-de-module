package com.elearning.resourceserver.domain.dto;

import lombok.Data;

@Data
public class AdminStatsDto {
    private Long totalOrganisations;
    private Long totalFormations;
    private Long totalUsers;
    private Long enrollmentsLastMonth;
}
