package com.elearning.resourceserver.domain.dto;

import lombok.Data;

@Data
public class ProgressUpdateRequest {
    private Integer watchedSeconds;
    private Integer progressSeconds;
    private Boolean completed;
}
