package com.taskflow.repository;

import com.taskflow.entity.Project;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByIdAndOwner_Id(UUID projectId, UUID ownerId);

    @EntityGraph(attributePaths = "owner")
    Page<Project> findAllByOwnerId(UUID ownerId, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    @Query(
            """
            SELECT p FROM Project p
            WHERE p.owner.id = :userId
            OR EXISTS (SELECT 1 FROM Task t WHERE t.project.id = p.id AND t.assignee.id = :userId)
            """)
    Page<Project> findAllVisibleToUser(@Param("userId") UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    @Query("SELECT p FROM Project p WHERE p.id = :id")
    Optional<Project> findDetailById(@Param("id") UUID id);
}
