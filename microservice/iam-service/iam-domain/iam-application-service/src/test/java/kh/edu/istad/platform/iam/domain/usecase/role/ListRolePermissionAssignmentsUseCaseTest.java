package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.RolePermission;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RolePermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.valueobject.Action;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionName;
import kh.edu.istad.platform.iam.domain.valueobject.Resource;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ListRolePermissionAssignmentsUseCaseTest {
    @Test
    void listsOnlyActiveUnexpiredPermissionsAssignedToTheRole() {
        UUID roleUuid = UUID.randomUUID(); Role role = new Role(); role.setId(1L); role.setUuid(roleUuid); role.setName(new RoleName("READERS"));
        Permission permission = new Permission(); permission.setId(2L); permission.setUuid(UUID.randomUUID()); permission.setName(new PermissionName("docs.read")); permission.setResource(new Resource("docs")); permission.setAction(new Action("read")); permission.setActive(true);
        RolePermission active = assignment(2L, null, Instant.now().plusSeconds(60));
        RolePermission revoked = assignment(2L, Instant.now(), null); RolePermission expired = assignment(2L, null, Instant.now().minusSeconds(5));
        RoleRepository roles = mock(RoleRepository.class); RolePermissionRepository assignments = mock(RolePermissionRepository.class); PermissionRepository permissions = mock(PermissionRepository.class);
        when(roles.findByUuid(roleUuid)).thenReturn(Optional.of(role)); when(assignments.findByRoleId(1L)).thenReturn(List.of(active, revoked, expired)); when(permissions.findAll()).thenReturn(List.of(permission));

        var result = new ListRolePermissionAssignmentsUseCase(roles, assignments, permissions).execute(roleUuid);

        assertEquals(1, result.size()); assertEquals(permission.getUuid(), result.getFirst().permissionUuid());
    }

    private RolePermission assignment(long permissionId, Instant revokedAt, Instant expiresAt) { RolePermission rp = new RolePermission(); rp.setPermissionId(permissionId); rp.setRevokedAt(revokedAt); rp.setExpiresAt(expiresAt); return rp; }
}
