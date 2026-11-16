package kh.edu.istad.platform.iam.domain.usecase.permission;

import kh.edu.istad.platform.iam.domain.dto.command.CreatePermissionCommand;
import kh.edu.istad.platform.iam.domain.dto.command.UpdatePermissionCommand;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.exception.PermissionNameConflictException;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.valueobject.Action;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionName;
import kh.edu.istad.platform.iam.domain.valueobject.Resource;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PermissionUseCasesTest {
    private final PermissionRepository permissions = mock(PermissionRepository.class);

    @Test
    void createsNormalizedConsistentPermission() {
        when(permissions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = new CreatePermissionUseCase(permissions).execute(
                new CreatePermissionCommand(" USER.permission.assign ", " User ", " Permission.Assign ", "desc"));
        assertEquals("user.permission.assign", result.name());
        assertEquals("user", result.resource());
        assertEquals("permission.assign", result.action());
        verify(permissions).save(any(Permission.class));
    }

    @Test
    void rejectsExistingAndMalformedDefinitionsBeforeWrite() {
        when(permissions.existsByName("user.read")).thenReturn(true);
        assertThrows(PermissionNameConflictException.class, () -> new CreatePermissionUseCase(permissions)
                .execute(new CreatePermissionCommand("user.read", "user", "read", null)));
        assertThrows(IllegalArgumentException.class, () -> new CreatePermissionUseCase(permissions)
                .execute(new CreatePermissionCommand("role.read", "user", "read", null)));
        assertThrows(IllegalArgumentException.class, () -> new CreatePermissionUseCase(permissions)
                .execute(new CreatePermissionCommand("user..read", "user", ".read", null)));
        assertThrows(IllegalArgumentException.class, () -> new CreatePermissionUseCase(permissions)
                .execute(new CreatePermissionCommand(" ", "user", "read", null)));
        assertThrows(IllegalArgumentException.class, () -> new CreatePermissionUseCase(permissions)
                .execute(new CreatePermissionCommand("a".repeat(101), "a", "read", null)));
        assertThrows(IllegalArgumentException.class, () -> new CreatePermissionUseCase(permissions)
                .execute(new CreatePermissionCommand("user.read", "user", "read", "x".repeat(501))));
        verify(permissions, never()).save(any());
    }

    @Test
    void updatesPermissionUsingSameDefinitionValidation() {
        Permission permission = permission("user.read", "user", "read", false, true);
        when(permissions.findByUuid(permission.getUuid())).thenReturn(Optional.of(permission));
        when(permissions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var useCase = new UpdatePermissionUseCase(permissions);
        assertEquals("user.update", useCase.execute(new UpdatePermissionCommand(permission.getUuid(), "user.update", "user", "update", "new")).name());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(
                new UpdatePermissionCommand(permission.getUuid(), "user.delete", "user", "manage", null)));
    }

    @Test
    void rejectsDuplicateNameOnUpdate() {
        Permission current = permission("user.read", "user", "read", false, true);
        when(permissions.findByUuid(current.getUuid())).thenReturn(Optional.of(current));
        when(permissions.existsByName("user.update")).thenReturn(true);
        assertThrows(PermissionNameConflictException.class, () -> new UpdatePermissionUseCase(permissions)
                .execute(new UpdatePermissionCommand(current.getUuid(), "user.update", "user", "update", null)));
        verify(permissions, never()).save(any());
    }

    @Test
    void protectsSystemPermissionLifecycleAndSoftDeletesCustomPermissions() {
        Permission system = permission("permission.read", "permission", "read", true, true);
        when(permissions.findByUuid(system.getUuid())).thenReturn(Optional.of(system));
        assertThrows(IllegalStateException.class, () -> new DeactivatePermissionUseCase(permissions).execute(system.getUuid()));
        assertThrows(IllegalStateException.class, () -> new DeletePermissionUseCase(permissions, mock(CurrentActorProvider.class)).execute(system.getUuid()));

        Permission custom = permission("document.read", "document", "read", false, true);
        when(permissions.findByUuid(custom.getUuid())).thenReturn(Optional.of(custom));
        when(permissions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        new DeactivatePermissionUseCase(permissions).execute(custom.getUuid());
        assertFalse(custom.isActive());
        new DeletePermissionUseCase(permissions, () -> "operator").execute(custom.getUuid());
        assertTrue(custom.isDeleted());
        assertNotNull(custom.getDeletedAt());
        assertEquals("operator", custom.getDeletedBy());
    }

    @Test
    void validatesPageSizeSortAndDelegatesDatabasePage() {
        when(permissions.findPage(any())).thenReturn(new kh.edu.istad.platform.iam.domain.dto.result.PageResult<>(java.util.List.of(), 0, 20, 41, 3, true, false));
        var useCase = new ListPermissionsUseCase(permissions);
        var result = useCase.execute(new kh.edu.istad.platform.iam.domain.dto.query.PermissionFilterQuery(
                "user", "user", "read", true, false, false, 0, 20, "createdAt", "DESC"));
        assertEquals(41, result.totalElements());
        assertEquals(3, result.totalPages());
        verify(permissions).findPage(any());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(query(0, 101, "name", "ASC")));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(query(0, 10, "unknown", "ASC")));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(query(0, 10, "name", "sideways")));
    }

    @Test
    void retrievesPermissionDetailsFromRepository() {
        Permission permission = permission("user.read", "user", "read", true, true);
        when(permissions.findByUuid(permission.getUuid())).thenReturn(Optional.of(permission));
        assertEquals("user.read", new GetPermissionUseCase(permissions).execute(permission.getUuid()).name());
    }

    private kh.edu.istad.platform.iam.domain.dto.query.PermissionFilterQuery query(int page, int size, String sort, String direction) {
        return new kh.edu.istad.platform.iam.domain.dto.query.PermissionFilterQuery(null, null, null, null, null, null, page, size, sort, direction);
    }

    private Permission permission(String name, String resource, String action, boolean system, boolean active) {
        Permission permission = new Permission(); permission.setId(1L); permission.setUuid(UUID.randomUUID());
        permission.setName(new PermissionName(name)); permission.setResource(new Resource(resource)); permission.setAction(new Action(action));
        permission.setSystem(system); permission.setActive(active); permission.setCreatedAt(Instant.now()); return permission;
    }
}
