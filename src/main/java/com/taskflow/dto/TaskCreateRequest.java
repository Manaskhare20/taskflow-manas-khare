package com.taskflow.dto;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class TaskCreateRequest {

    @NotBlank(message = "title is required")
    @Size(max = 500, message = "title must be at most 500 characters")
    private String title;

    @Size(max = 20_000, message = "description must be at most 20000 characters")
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private UUID assigneeId;

    private LocalDate dueDate;
}
