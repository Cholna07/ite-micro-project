package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRoleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RevokeRoleFromUserUseCaseTest {
    @Test
    void revokesExistingAssignmentAndPreservesItAsHistory() {
        UUID userUuid = UUID.randomUUID();
        UUID roleUuid = UUID.randomUUID();
        User user = new User();
        user.setId(1L);
        Role role = new Role();
        role.setId(2L);
        UserRole assignment = new UserRole();
        assignment.setUserId(1L);
        assignment.setRoleId(2L);
        UserRepository users = mock(UserRepository.class);
        UserRoleRepository assignments = mock(UserRoleRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        CurrentActorProvider actors = mock(CurrentActorProvider.class);
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(roles.findByUuid(roleUuid)).thenReturn(Optional.of(role));
        when(assignments.findActiveAssignment(1L, 2L)).thenReturn(Optional.of(assignment));
        when(actors.getCurrentActor()).thenReturn("admin");

        new RevokeRoleFromUserUseCase(users, assignments, roles, actors).execute(userUuid, roleUuid, "reviewed");

        assertNotNull(assignment.getRevokedAt());
        assertEquals("admin", assignment.getRevokedBy());
        assertEquals("reviewed", assignment.getReason());
        verify(assignments).save(assignment);
    }
}
