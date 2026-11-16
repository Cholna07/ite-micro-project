package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.RolePermission;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RolePermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RevokePermissionFromRoleUseCaseTest {
    @Test
    void softRevokesAssignmentAndRetainsActorAndReason() {
        UUID roleUuid = UUID.randomUUID(), permissionUuid = UUID.randomUUID();
        Role role = new Role(); role.setId(1L);
        Permission permission = new Permission(); permission.setId(2L);
        RolePermission assignment = new RolePermission();
        RoleRepository roles = mock(RoleRepository.class); PermissionRepository permissions = mock(PermissionRepository.class);
        RolePermissionRepository assignments = mock(RolePermissionRepository.class); CurrentActorProvider actors = mock(CurrentActorProvider.class);
        when(roles.findByUuid(roleUuid)).thenReturn(Optional.of(role)); when(permissions.findByUuid(permissionUuid)).thenReturn(Optional.of(permission));
        when(assignments.findActiveAssignment(1L, 2L)).thenReturn(Optional.of(assignment)); when(actors.getCurrentActor()).thenReturn("admin");

        new RevokePermissionFromRoleUseCase(roles, permissions, assignments, actors).execute(roleUuid, permissionUuid, "removed");

        assertNotNull(assignment.getRevokedAt()); assertEquals("admin", assignment.getRevokedBy()); assertEquals("removed", assignment.getReason());
        verify(assignments).save(assignment);
    }
}
