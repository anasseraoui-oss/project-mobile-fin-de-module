package com.elearning.resourceserver.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstructorDashboardDto {
    private InstructorProfileDto instructor;
    private InstructorStatsDto stats;
}
