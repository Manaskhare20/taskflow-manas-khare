package com.taskflow.service;

import com.taskflow.authorization.AuthorizationService;
import com.taskflow.dto.TaskCreateRequest;
import com.taskflow.dto.TaskPatchRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import com.taskflow.entity.User;
import com.taskflow.exception.ApiException;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final AuthorizationService authorizationService;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<TaskResponse> listInProject(
            UUID currentUserId, UUID projectId, String statusParam, String assigneeParam, Pageable pageable) {
        authorizationService.requireProjectMember(projectId, currentUserId);
        TaskStatus status = parseStatus(statusParam);
        UUID assigneeId = parseAssignee(assigneeParam);
        return taskRepository
                .findByProjectFiltered(projectId, status, assigneeId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public TaskResponse create(UUID currentUserId, UUID projectId, TaskCreateRequest request) {
        authorizationService.requireProjectOwner(projectId, currentUserId);
        Project project = projectRepository
                .findDetailById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not found"));
        User creator = userRepository.getReferenceById(currentUserId);
        User assignee = resolveAssignee(request.getAssigneeId());
        Task task = Task.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.todo)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.medium)
                .project(project)
                .assignee(assignee)
                .creator(creator)
                .dueDate(request.getDueDate())
                .build();
        taskRepository.save(task);
        log.info(
                "action=task_created taskId={} projectId={} userId={}",
                task.getId(),
                projectId,
                currentUserId);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse patch(UUID currentUserId, UUID taskId, TaskPatchRequest request) {
        validateTaskPatch(request);
        Task task = taskRepository
                .findDetailById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not found"));
        authorizationService.requireCanModifyTask(task, currentUserId);
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (Boolean.TRUE.equals(request.getClearAssignee())) {
            task.setAssignee(null);
        } else if (request.getAssigneeId() != null) {
            task.setAssignee(resolveAssignee(request.getAssigneeId()));
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        log.info("action=task_updated taskId={} userId={}", taskId, currentUserId);
        return toResponse(task);
    }

    @Transactional
    public void delete(UUID currentUserId, UUID taskId) {
        Task task = taskRepository
                .findDetailById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not found"));
        authorizationService.requireCanDeleteTask(task, currentUserId);
        taskRepository.delete(task);
        log.info("action=task_deleted taskId={} userId={}", taskId, currentUserId);
    }

    private User resolveAssignee(UUID assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository
                .findById(assigneeId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "validation failed",
                        Map.of("assigneeId", "user does not exist")));
    }

    private static void validateTaskPatch(TaskPatchRequest request) {
        if (!hasPatchChanges(request)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "validation failed",
                    Map.of("body", "at least one field must be provided"));
        }
        if (Boolean.TRUE.equals(request.getClearAssignee()) && request.getAssigneeId() != null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "validation failed",
                    Map.of("assigneeId", "cannot set assignee and clearAssignee together"));
        }
    }

    private static TaskStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return TaskStatus.valueOf(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "validation failed",
                    Map.of("status", "must be one of todo, in_progress, done"));
        }
    }

    private static UUID parseAssignee(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "validation failed",
                    Map.of("assignee", "must be a valid UUID"));
        }
    }

    private static boolean hasPatchChanges(TaskPatchRequest request) {
        return request.getTitle() != null
                || request.getDescription() != null
                || request.getStatus() != null
                || request.getPriority() != null
                || request.getAssigneeId() != null
                || Boolean.TRUE.equals(request.getClearAssignee())
                || request.getDueDate() != null;
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .projectId(task.getProject().getId())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .creatorId(task.getCreator().getId())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
