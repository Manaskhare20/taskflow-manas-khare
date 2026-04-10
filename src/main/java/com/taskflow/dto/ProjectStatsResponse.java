package com.taskflow.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProjectStatsResponse {
    long todo;
    long inProgress;
    long done;
    long total;
}
