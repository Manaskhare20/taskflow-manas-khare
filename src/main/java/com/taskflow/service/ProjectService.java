package com.taskflow.service;

import com.taskflow.authorization.AuthorizationService;
import com.taskflow.dto.ProjectCreateRequest;
import com.taskflow.dto.ProjectPatchRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.dto.ProjectStatsResponse;
import com.taskflow.entity.Project;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public Page<ProjectResponse> listVisibleForUser(UUID currentUserId, Pageable pageable) {
        return projectRepository.findAllVisibleToUser(currentUserId, pageable).map(this::toResponse);
    }

    @Transactional
    public ProjectResponse create(UUID currentUserId, ProjectCreateRequest request) {
        User owner = userRepository.getReferenceById(currentUserId);
        Project project = Project.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .owner(owner)
                .build();
        projectRepository.save(project);
        log.info("action=project_created projectId={} ownerId={}", project.getId(), currentUserId);
        return toResponse(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(UUID currentUserId, UUID projectId) {
        authorizationService.requireProjectMember(projectId, currentUserId);
        Project project = projectRepository
                .findDetailById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not found"));
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse patch(UUID currentUserId, UUID projectId, ProjectPatchRequest request) {
        validateProjectPatch(request);
        authorizationService.requireProjectOwner(projectId, currentUserId);
        Project project = projectRepository
                .findDetailById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not found"));
        if (request.getName() != null) {
            project.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        log.info("action=project_updated projectId={} userId={}", projectId, currentUserId);
        return toResponse(project);
    }

    @Transactional
    public void delete(UUID currentUserId, UUID projectId) {
        authorizationService.requireProjectOwner(projectId, currentUserId);
        Project project = projectRepository
                .findDetailById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not found"));
        projectRepository.delete(project);
        log.info("action=project_deleted projectId={} userId={}", projectId, currentUserId);
    }

    @Transactional(readOnly = true)
    public ProjectStatsResponse stats(UUID currentUserId, UUID projectId) {
        authorizationService.requireProjectMember(projectId, currentUserId);
        long todo = taskRepository.countByProject_IdAndStatus(projectId, TaskStatus.todo);
        long inProgress = taskRepository.countByProject_IdAndStatus(projectId, TaskStatus.in_progress);
        long done = taskRepository.countByProject_IdAndStatus(projectId, TaskStatus.done);
        long total = taskRepository.countByProject_Id(projectId);
        return ProjectStatsResponse.builder()
                .todo(todo)
                .inProgress(inProgress)
                .done(done)
                .total(total)
                .build();
    }

    private static void validateProjectPatch(ProjectPatchRequest request) {
        if (request.getName() == null && request.getDescription() == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "validation failed",
                    Map.of("body", "at least one of name or description must be provided"));
        }
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwner().getId())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
