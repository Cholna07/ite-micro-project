package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserPermission;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserPermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RevokePermissionFromUserUseCaseTest {
    @Test
    void softRevokesAnActiveDirectAssignmentAndRetainsAuditMetadata() {
        UUID userUuid = UUID.randomUUID();
        UUID permissionUuid = UUID.randomUUID();
        User user = new User();
        user.setId(2L);
        Permission permission = new Permission();
        permission.setId(3L);
        UserPermission assignment = new UserPermission();
        assignment.setUserId(2L);
        assignment.setPermissionId(3L);
        UserRepository users = mock(UserRepository.class);
        PermissionRepository permissions = mock(PermissionRepository.class);
        UserPermissionRepository assignments = mock(UserPermissionRepository.class);
        CurrentActorProvider actors = mock(CurrentActorProvider.class);
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(permissions.findByUuid(permissionUuid)).thenReturn(Optional.of(permission));
        when(assignments.findActiveAssignment(2L, 3L)).thenReturn(Optional.of(assignment));
        when(actors.getCurrentActor()).thenReturn("admin");

        new RevokePermissionFromUserUseCase(users, permissions, assignments, actors)
                .execute(userUuid, permissionUuid, "reviewed");

        assertNotNull(assignment.getRevokedAt());
        assertEquals("admin", assignment.getRevokedBy());
        assertEquals("reviewed", assignment.getReason());
        verify(assignments).save(assignment);
    }
}
