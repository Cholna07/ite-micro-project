package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.GrantPermissionsToUserCommand;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.exception.PermissionAlreadyAssignedException;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserPermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionEffect;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionName;
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
class GrantPermissionsToUserUseCaseTest {
    @Mock UserRepository users;
    @Mock PermissionRepository permissions;
    @Mock UserPermissionRepository assignments;
    @Mock CurrentActorProvider actors;

    private UUID userUuid;
    private User user;
    private Permission first;
    private Permission second;
    private GrantPermissionsToUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userUuid = UUID.randomUUID();
        user = new User();
        user.setId(4L);
        user.setUuid(userUuid);
        user.setEnabled(true);
        first = permission(10L);
        second = permission(11L);
        useCase = new GrantPermissionsToUserUseCase(users, permissions, assignments, actors);
        lenient().when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        lenient().when(permissions.findByUuid(first.getUuid())).thenReturn(Optional.of(first));
        lenient().when(permissions.findByUuid(second.getUuid())).thenReturn(Optional.of(second));
        lenient().when(assignments.existsActiveAssignment(4L, 10L)).thenReturn(false);
        lenient().when(assignments.existsActiveAssignment(4L, 11L)).thenReturn(false);
        lenient().when(assignments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(actors.getCurrentActor()).thenReturn("admin");
    }

    @Test
    void assignsSingleGrantAndDenyEffects() {
        var grant = useCase.execute(command(List.of(first.getUuid()), "GRANT"));
        var deny = useCase.execute(command(List.of(second.getUuid()), "DENY"));

        assertEquals("GRANT", grant.getFirst().effect());
        assertEquals("DENY", deny.getFirst().effect());
        verify(assignments).save(argThat(value -> value.getPermissionId().equals(10L)
                && value.getEffect() == PermissionEffect.GRANT));
        verify(assignments).save(argThat(value -> value.getPermissionId().equals(11L)
                && value.getEffect() == PermissionEffect.DENY));
    }

    @Test
    void assignsMultiplePermissionsWithOneEffect() {
        var result = useCase.execute(command(List.of(first.getUuid(), second.getUuid()), "grant"));

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(item -> item.effect().equals("GRANT")));
        verify(assignments, times(2)).save(any());
    }

    @Test
    void validatesEveryPermissionBeforeMakingAnyWrite() {
        UUID missingUuid = UUID.randomUUID();
        when(permissions.findByUuid(missingUuid)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(command(List.of(first.getUuid(), missingUuid), "GRANT")));

        verifyNoWrites();
    }

    @Test
    void rejectsInactiveAndDuplicatePermissionsBeforeWriting() {
        Permission inactive = permission(12L);
        inactive.setActive(false);
        when(permissions.findByUuid(inactive.getUuid())).thenReturn(Optional.of(inactive));
        assertThrows(IllegalStateException.class,
                () -> useCase.execute(command(List.of(first.getUuid(), inactive.getUuid()), "GRANT")));
        verifyNoWrites();

        assertThrows(PermissionAlreadyAssignedException.class,
                () -> useCase.execute(command(List.of(first.getUuid(), first.getUuid()), "GRANT")));
        verifyNoWrites();
    }

    @Test
    void rejectsExistingActiveAssignmentAndAllowsExpiredHistoryToBeReassigned() {
        when(assignments.existsActiveAssignment(4L, 10L)).thenReturn(true);
        assertThrows(PermissionAlreadyAssignedException.class,
                () -> useCase.execute(command(List.of(first.getUuid()), "GRANT")));
        verifyNoWrites();

        reset(assignments);
        when(assignments.existsActiveAssignment(4L, 10L)).thenReturn(false);
        when(assignments.revokeExpiredAssignments(eq(4L), eq(10L), any(Instant.class), eq("admin"))).thenReturn(1);
        when(assignments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(actors.getCurrentActor()).thenReturn("admin");

        assertEquals(1, useCase.execute(command(List.of(first.getUuid()), "DENY")).size());
        InOrder inOrder = inOrder(assignments);
        inOrder.verify(assignments).revokeExpiredAssignments(eq(4L), eq(10L), any(Instant.class), eq("admin"));
        inOrder.verify(assignments).save(any());
    }

    @Test
    void operationIsTransactionalSoPersistenceErrorsRollbackTheBatch() throws Exception {
        assertNotNull(GrantPermissionsToUserUseCase.class
                .getMethod("execute", GrantPermissionsToUserCommand.class)
                .getAnnotation(Transactional.class));
    }

    private GrantPermissionsToUserCommand command(List<UUID> ids, String effect) {
        return new GrantPermissionsToUserCommand(userUuid, ids, effect, null, "test assignment");
    }

    private Permission permission(long id) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setUuid(UUID.randomUUID());
        permission.setName(new PermissionName("permission-" + id));
        permission.setActive(true);
        return permission;
    }

    private void verifyNoWrites() {
        verify(assignments, never()).save(any());
        verify(assignments, never()).revokeExpiredAssignments(anyLong(), anyLong(), any(), anyString());
    }
}
