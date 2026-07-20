package com.aicontentstudio.repository;

import com.aicontentstudio.entity.Workspace;
import com.aicontentstudio.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Page<Workspace> findByOwner(User owner, Pageable pageable);

    Page<Workspace> findByActive(boolean active, Pageable pageable);

    @Query("SELECT w FROM Workspace w WHERE w.owner = :user OR EXISTS " +
           "(SELECT wm FROM WorkspaceMember wm WHERE wm.workspace = w AND wm.user = :user AND wm.accepted = true)")
    Page<Workspace> findAllAccessibleByUser(@Param("user") User user, Pageable pageable);

    boolean existsByNameAndOwner(String name, User owner);

    long countByOwner(User owner);
}
