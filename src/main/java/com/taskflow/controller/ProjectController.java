package com.taskflow.controller;

import com.taskflow.dto.PageResponse;
import com.taskflow.dto.ProjectCreateRequest;
import com.taskflow.dto.ProjectPatchRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.dto.ProjectStatsResponse;
import com.taskflow.dto.TaskCreateRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.security.SecurityUtils;
import com.taskflow.service.ProjectService;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    @GetMapping
    public PageResponse<ProjectResponse> listProjects(
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(projectService.listVisibleForUser(SecurityUtils.currentUserId(), pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody ProjectCreateRequest request) {
        return projectService.create(SecurityUtils.currentUserId(), request);
    }

    @GetMapping("/{id}")
    public ProjectResponse getProject(@PathVariable("id") UUID id) {
        return projectService.get(SecurityUtils.currentUserId(), id);
    }

    @PatchMapping("/{id}")
    public ProjectResponse patchProject(
            @PathVariable("id") UUID id, @Valid @RequestBody ProjectPatchRequest request) {
        return projectService.patch(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable("id") UUID id) {
        projectService.delete(SecurityUtils.currentUserId(), id);
    }

    @GetMapping("/{id}/stats")
    public ProjectStatsResponse projectStats(@PathVariable("id") UUID id) {
        return projectService.stats(SecurityUtils.currentUserId(), id);
    }

    @GetMapping("/{id}/tasks")
    public PageResponse<TaskResponse> listTasks(
            @PathVariable("id") UUID id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assignee,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(
                taskService.listInProject(SecurityUtils.currentUserId(), id, status, assignee, pageable));
    }

    @PostMapping("/{id}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(
            @PathVariable("id") UUID id, @Valid @RequestBody TaskCreateRequest request) {
        return taskService.create(SecurityUtils.currentUserId(), id, request);
    }
}
