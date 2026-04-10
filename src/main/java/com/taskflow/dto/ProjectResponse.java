package com.taskflow.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProjectResponse {
    UUID id;
    String name;
    String description;
    UUID ownerId;
    Instant createdAt;
}
