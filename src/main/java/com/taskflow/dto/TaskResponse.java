package com.taskflow.dto;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TaskResponse {
    UUID id;
    String title;
    String description;
    TaskStatus status;
    TaskPriority priority;
    UUID projectId;
    UUID assigneeId;
    UUID creatorId;
    LocalDate dueDate;
    Instant createdAt;
    Instant updatedAt;
}
