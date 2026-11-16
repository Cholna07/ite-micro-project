package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserPermission;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserPermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionEffect;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ListUserPermissionAssignmentsUseCaseTest {
    @Test
    void listsOnlyUnrevokedUnexpiredDirectAssignmentsIncludingTheirEffect() {
        UUID userUuid = UUID.randomUUID();
        User user = new User();
        user.setId(5L);
        Permission permission = new Permission();
        permission.setId(8L);
        permission.setUuid(UUID.randomUUID());
        permission.setName(new PermissionName("files.read"));
        permission.setActive(true);
        UserPermission grant = assignment(8L, PermissionEffect.GRANT, null, Instant.now().plusSeconds(600));
        UserPermission deny = assignment(8L, PermissionEffect.DENY, null, null);
        UserPermission revoked = assignment(8L, PermissionEffect.GRANT, Instant.now(), null);
        UserPermission expired = assignment(8L, PermissionEffect.GRANT, null, Instant.now().minusSeconds(5));
        UserRepository users = mock(UserRepository.class);
        UserPermissionRepository assignments = mock(UserPermissionRepository.class);
        PermissionRepository permissions = mock(PermissionRepository.class);
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(assignments.findByUserId(5L)).thenReturn(List.of(grant, deny, revoked, expired));
        when(permissions.findAll()).thenReturn(List.of(permission));

        var result = new ListUserPermissionAssignmentsUseCase(users, assignments, permissions).execute(userUuid);

        assertEquals(2, result.size());
        assertEquals(List.of("GRANT", "DENY"), result.stream().map(item -> item.effect()).toList());
        verify(assignments).findByUserId(5L);
    }

    private UserPermission assignment(long permissionId, PermissionEffect effect, Instant revokedAt, Instant expiresAt) {
        UserPermission assignment = new UserPermission();
        assignment.setPermissionId(permissionId);
        assignment.setEffect(effect);
        assignment.setAssignedAt(Instant.now().minusSeconds(30));
        assignment.setRevokedAt(revokedAt);
        assignment.setExpiresAt(expiresAt);
        return assignment;
    }
}
