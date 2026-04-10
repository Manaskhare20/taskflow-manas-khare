package com.taskflow.repository;

import com.taskflow.entity.Task;
import com.taskflow.entity.TaskStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    boolean existsByProject_IdAndAssignee_Id(UUID projectId, UUID assigneeId);

    long countByProject_IdAndStatus(UUID projectId, TaskStatus status);

    long countByProject_Id(UUID projectId);

    @EntityGraph(attributePaths = {"project", "project.owner", "assignee", "creator"})
    @Query(
            """
            SELECT t FROM Task t
            WHERE t.project.id = :projectId
            AND (:status IS NULL OR t.status = :status)
            AND (:assigneeId IS NULL OR (t.assignee IS NOT NULL AND t.assignee.id = :assigneeId))
            """)
    Page<Task> findByProjectFiltered(
            @Param("projectId") UUID projectId,
            @Param("status") TaskStatus status,
            @Param("assigneeId") UUID assigneeId,
            Pageable pageable);

    @EntityGraph(attributePaths = {"project", "project.owner", "assignee", "creator"})
    @Query("SELECT t FROM Task t WHERE t.id = :id")
    Optional<Task> findDetailById(@Param("id") UUID id);
}
