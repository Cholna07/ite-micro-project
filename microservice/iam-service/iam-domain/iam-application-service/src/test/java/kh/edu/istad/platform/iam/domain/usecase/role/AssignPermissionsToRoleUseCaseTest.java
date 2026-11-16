package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.dto.command.AssignPermissionsToRoleCommand;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.exception.RolePermissionAlreadyAssignedException;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RolePermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionName;
import kh.edu.istad.platform.iam.domain.valueobject.Resource;
import kh.edu.istad.platform.iam.domain.valueobject.Action;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignPermissionsToRoleUseCaseTest {
    @Mock RoleRepository roles;
    @Mock PermissionRepository permissions;
    @Mock RolePermissionRepository assignments;
    @Mock CurrentActorProvider actors;
    private UUID roleUuid;
    private Role role;
    private Permission first;
    private Permission second;
    private AssignPermissionsToRoleUseCase useCase;

    @BeforeEach
    void setUp() {
        roleUuid = UUID.randomUUID(); role = new Role(); role.setId(2L); role.setUuid(roleUuid); role.setName(new RoleName("READER")); role.setActive(true);
        first = permission(4L); second = permission(5L);
        useCase = new AssignPermissionsToRoleUseCase(roles, permissions, assignments, actors);
        lenient().when(roles.findByUuid(roleUuid)).thenReturn(Optional.of(role));
        lenient().when(permissions.findByUuid(first.getUuid())).thenReturn(Optional.of(first));
        lenient().when(permissions.findByUuid(second.getUuid())).thenReturn(Optional.of(second));
        lenient().when(assignments.existsActiveAssignment(2L, 4L)).thenReturn(false);
        lenient().when(assignments.existsActiveAssignment(2L, 5L)).thenReturn(false);
        lenient().when(assignments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(actors.getCurrentActor()).thenReturn("admin");
    }

    @Test
    void assignsOneAndMultiplePermissions() {
        assertEquals(1, useCase.execute(command(List.of(first.getUuid()))).size());
        var batch = useCase.execute(command(List.of(first.getUuid(), second.getUuid())));
        assertEquals(2, batch.size());
        verify(assignments, times(3)).save(any());
    }

    @Test
    void rejectsMissingInactiveDuplicateOrAlreadyActivePermissionBeforeWriting() {
        UUID missing = UUID.randomUUID(); when(permissions.findByUuid(missing)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command(List.of(first.getUuid(), missing))));
        Permission inactive = permission(6L); inactive.setActive(false);
        when(permissions.findByUuid(inactive.getUuid())).thenReturn(Optional.of(inactive));
        assertThrows(IllegalStateException.class, () -> useCase.execute(command(List.of(first.getUuid(), inactive.getUuid()))));
        assertThrows(RolePermissionAlreadyAssignedException.class, () -> useCase.execute(command(List.of(first.getUuid(), first.getUuid()))));
        when(assignments.existsActiveAssignment(2L, 4L)).thenReturn(true);
        assertThrows(RolePermissionAlreadyAssignedException.class, () -> useCase.execute(command(List.of(first.getUuid()))));
        verifyNoWrites();
    }

    @Test
    void prevalidatesBatchAndReleasesExpiredHistoryBeforeReassignment() {
        UUID missing = UUID.randomUUID(); when(permissions.findByUuid(missing)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command(List.of(first.getUuid(), missing))));
        verifyNoWrites();

        reset(assignments);
        when(assignments.existsActiveAssignment(2L, 4L)).thenReturn(false);
        when(assignments.revokeExpiredAssignments(eq(2L), eq(4L), any(Instant.class), eq("admin"))).thenReturn(1);
        when(assignments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(actors.getCurrentActor()).thenReturn("admin");
        assertEquals(1, useCase.execute(command(List.of(first.getUuid()))).size());
        InOrder order = inOrder(assignments);
        order.verify(assignments).revokeExpiredAssignments(eq(2L), eq(4L), any(Instant.class), eq("admin"));
        order.verify(assignments).save(any());
    }

    @Test
    void assignmentIsTransactionalToRollbackPersistenceErrorsAcrossTheBatch() throws Exception {
        assertNotNull(AssignPermissionsToRoleUseCase.class.getMethod("execute", AssignPermissionsToRoleCommand.class).getAnnotation(Transactional.class));
    }

    private AssignPermissionsToRoleCommand command(List<UUID> ids) { return new AssignPermissionsToRoleCommand(roleUuid, ids, null, "test"); }
    private Permission permission(long id) { Permission p = new Permission(); p.setId(id); p.setUuid(UUID.randomUUID()); p.setName(new PermissionName("p" + id)); p.setResource(new Resource("resource")); p.setAction(new Action("read")); p.setActive(true); return p; }
    private void verifyNoWrites() { verify(assignments, never()).save(any()); verify(assignments, never()).revokeExpiredAssignments(anyLong(), anyLong(), any(), anyString()); }
}
