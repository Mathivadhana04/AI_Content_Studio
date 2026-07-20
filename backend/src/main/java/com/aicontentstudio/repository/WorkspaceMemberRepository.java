package com.aicontentstudio.repository;

import com.aicontentstudio.entity.WorkspaceMember;
import com.aicontentstudio.entity.Workspace;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.enums.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    List<WorkspaceMember> findByWorkspace(Workspace workspace);

    List<WorkspaceMember> findByWorkspaceAndAccepted(Workspace workspace, boolean accepted);

    Optional<WorkspaceMember> findByWorkspaceAndUser(Workspace workspace, User user);

    boolean existsByWorkspaceAndUser(Workspace workspace, User user);

    List<WorkspaceMember> findByUserAndAccepted(User user, boolean accepted);

    void deleteByWorkspaceAndUser(Workspace workspace, User user);

    long countByWorkspace(Workspace workspace);

    List<WorkspaceMember> findByWorkspaceAndRole(Workspace workspace, WorkspaceRole role);
}
