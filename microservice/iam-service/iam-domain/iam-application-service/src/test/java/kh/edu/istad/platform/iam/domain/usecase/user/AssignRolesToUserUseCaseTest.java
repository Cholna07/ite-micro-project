package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.AssignRolesToUserCommand;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.exception.RoleAlreadyAssignedException;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRoleRepository;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignRolesToUserUseCaseTest {
    @Mock UserRepository users;
    @Mock RoleRepository roles;
    @Mock UserRoleRepository assignments;
    @Mock CurrentActorProvider actors;

    private UUID userUuid;
    private User user;
    private Role firstRole;
    private Role secondRole;
    private AssignRolesToUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userUuid = UUID.randomUUID();
        user = new User();
        user.setId(7L);
        user.setUuid(userUuid);
        user.setEnabled(true);
        firstRole = role(11L);
        secondRole = role(12L);
        useCase = new AssignRolesToUserUseCase(users, roles, assignments, actors);
        lenient().when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        lenient().when(actors.getCurrentActor()).thenReturn("admin");
        lenient().when(assignments.existsActiveAssignment(7L, 11L)).thenReturn(false);
        lenient().when(assignments.existsActiveAssignment(7L, 12L)).thenReturn(false);
        lenient().when(assignments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(roles.findByUuid(firstRole.getUuid())).thenReturn(Optional.of(firstRole));
        lenient().when(roles.findByUuid(secondRole.getUuid())).thenReturn(Optional.of(secondRole));
    }

    @Test
    void assignsOneRoleAndClosesExpiredHistoryBeforeSaving() {
        var result = useCase.execute(command(List.of(firstRole.getUuid())));

        assertEquals(1, result.size());
        assertEquals(firstRole.getUuid(), result.getFirst().roleUuid());
        verify(assignments).revokeExpiredAssignments(eq(7L), eq(11L), any(Instant.class), eq("admin"));
        verify(assignments).save(argThat(assignment -> assignment.getUserId().equals(7L)
                && assignment.getRoleId().equals(11L) && assignment.getRevokedAt() == null));
    }

    @Test
    void assignsMultipleRolesAsOneUseCaseOperation() {
        var result = useCase.execute(command(List.of(firstRole.getUuid(), secondRole.getUuid())));

        assertEquals(List.of(firstRole.getUuid(), secondRole.getUuid()), result.stream().map(r -> r.roleUuid()).toList());
        verify(assignments, times(2)).save(any());
        verify(assignments, times(2)).revokeExpiredAssignments(anyLong(), anyLong(), any(), anyString());
    }

    @Test
    void rejectsNonexistentOrInactiveRolesBeforeWritingAnyAssignment() {
        UUID missingRoleUuid = UUID.randomUUID();
        when(roles.findByUuid(missingRoleUuid)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(command(List.of(firstRole.getUuid(), missingRoleUuid))));
        verifyNoWrites();

        Role inactive = role(13L);
        inactive.setActive(false);
        when(roles.findByUuid(inactive.getUuid())).thenReturn(Optional.of(inactive));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(command(List.of(firstRole.getUuid(), inactive.getUuid()))));
        verifyNoWrites();
    }

    @Test
    void rejectsExistingAndDuplicateRequestAssignmentsBeforeWriting() {
        when(assignments.existsActiveAssignment(7L, 11L)).thenReturn(true);
        assertThrows(RoleAlreadyAssignedException.class,
                () -> useCase.execute(command(List.of(firstRole.getUuid()))));
        verifyNoWrites();

        assertThrows(RoleAlreadyAssignedException.class,
                () -> useCase.execute(command(List.of(firstRole.getUuid(), firstRole.getUuid()))));
        verifyNoWrites();
    }

    @Test
    void prevalidatesWholeBatchSoAnInvalidLaterRoleLeavesNoPartialWrites() {
        UUID missingRoleUuid = UUID.randomUUID();
        when(roles.findByUuid(missingRoleUuid)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(command(List.of(firstRole.getUuid(), missingRoleUuid))));

        verifyNoWrites();
    }

    @Test
    void assignmentMethodIsTransactionalToRollbackPersistenceFailuresAcrossBatch() throws Exception {
        assertNotNull(AssignRolesToUserUseCase.class.getMethod("execute", AssignRolesToUserCommand.class)
                .getAnnotation(Transactional.class));
    }

    private AssignRolesToUserCommand command(List<UUID> roleUuids) {
        return new AssignRolesToUserCommand(userUuid, roleUuids, null, "assigned by test");
    }

    private Role role(long id) {
        Role role = new Role();
        role.setId(id);
        role.setUuid(UUID.randomUUID());
        role.setName(new RoleName("role-" + id));
        role.setActive(true);
        return role;
    }

    private void verifyNoWrites() {
        verify(assignments, never()).save(any());
        verify(assignments, never()).revokeExpiredAssignments(anyLong(), anyLong(), any(), anyString());
    }
}
