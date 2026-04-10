package com.taskflow.controller;

import com.taskflow.dto.TaskPatchRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.security.SecurityUtils;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final TaskService taskService;

    @PatchMapping("/{id}")
    public TaskResponse patchTask(@PathVariable("id") UUID id, @Valid @RequestBody TaskPatchRequest request) {
        return taskService.patch(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable("id") UUID id) {
        taskService.delete(SecurityUtils.currentUserId(), id);
    }
}
