package com.taskflow.authorization;

import com.taskflow.entity.Task;
import com.taskflow.exception.ApiException;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Centralized authorization checks for projects and tasks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public boolean isProjectOwner(UUID projectId, UUID userId) {
        return projectRepository.existsByIdAndOwner_Id(projectId, userId);
    }

    /**
     * Member = project owner OR user is assignee on at least one task in the project.
     */
    @Transactional(readOnly = true)
    public boolean isProjectMember(UUID projectId, UUID userId) {
        return isProjectOwner(projectId, userId)
                || taskRepository.existsByProject_IdAndAssignee_Id(projectId, userId);
    }

    /**
     * Task may be modified by project owner, task assignee, or task creator.
     */
    public boolean canModifyTask(Task task, UUID userId) {
        UUID ownerId = task.getProject().getOwner().getId();
        if (userId.equals(ownerId)) {
            return true;
        }
        if (task.getAssignee() != null && userId.equals(task.getAssignee().getId())) {
            return true;
        }
        return userId.equals(task.getCreator().getId());
    }

    @Transactional(readOnly = true)
    public boolean canModifyTask(UUID taskId, UUID userId) {
        return taskRepository
                .findDetailById(taskId)
                .map(task -> canModifyTask(task, userId))
                .orElse(false);
    }

    public boolean canDeleteTask(Task task, UUID userId) {
        UUID ownerId = task.getProject().getOwner().getId();
        return userId.equals(ownerId) || userId.equals(task.getCreator().getId());
    }

    @Transactional(readOnly = true)
    public boolean canDeleteTask(UUID taskId, UUID userId) {
        return taskRepository
                .findDetailById(taskId)
                .map(task -> canDeleteTask(task, userId))
                .orElse(false);
    }

    public void requireProjectExists(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "not found");
        }
    }

    public void requireProjectMember(UUID projectId, UUID userId) {
        requireProjectExists(projectId);
        if (!isProjectMember(projectId, userId)) {
            log.debug("Denied project member access projectId={} userId={}", projectId, userId);
            throw new ApiException(HttpStatus.FORBIDDEN, "forbidden");
        }
    }

    public void requireProjectOwner(UUID projectId, UUID userId) {
        requireProjectExists(projectId);
        if (!isProjectOwner(projectId, userId)) {
            log.debug("Denied project owner access projectId={} userId={}", projectId, userId);
            throw new ApiException(HttpStatus.FORBIDDEN, "forbidden");
        }
    }

    public void requireCanModifyTask(Task task, UUID userId) {
        if (!canModifyTask(task, userId)) {
            log.debug("Denied task modify taskId={} userId={}", task.getId(), userId);
            throw new ApiException(HttpStatus.FORBIDDEN, "forbidden");
        }
    }

    public void requireCanDeleteTask(Task task, UUID userId) {
        if (!canDeleteTask(task, userId)) {
            log.debug("Denied task delete taskId={} userId={}", task.getId(), userId);
            throw new ApiException(HttpStatus.FORBIDDEN, "forbidden");
        }
    }
}
