package com.taskflow.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectPatchRequest {

    @Size(max = 255, message = "name must be at most 255 characters")
    private String name;

    @Size(max = 10_000, message = "description must be at most 10000 characters")
    private String description;
}
