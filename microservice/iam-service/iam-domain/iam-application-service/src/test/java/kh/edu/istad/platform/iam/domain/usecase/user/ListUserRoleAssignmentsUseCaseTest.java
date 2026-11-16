package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRoleRepository;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ListUserRoleAssignmentsUseCaseTest {
    @Test
    void listsOnlyUnrevokedUnexpiredAssignmentsForTheRequestedUser() {
        UUID userUuid = UUID.randomUUID();
        User user = new User();
        user.setId(20L);
        Role role = new Role();
        role.setId(30L);
        role.setUuid(UUID.randomUUID());
        role.setName(new RoleName("operator"));
        role.setActive(true);

        UserRole active = assignment(20L, 30L, null, Instant.now().plusSeconds(3600));
        UserRole revoked = assignment(20L, 30L, Instant.now(), null);
        UserRole expired = assignment(20L, 30L, null, Instant.now().minusSeconds(5));
        UserRepository users = mock(UserRepository.class);
        UserRoleRepository assignments = mock(UserRoleRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(assignments.findByUserId(20L)).thenReturn(List.of(active, revoked, expired));
        when(roles.findAll()).thenReturn(List.of(role));

        var result = new ListUserRoleAssignmentsUseCase(users, assignments, roles).execute(userUuid);

        assertEquals(1, result.size());
        assertEquals(role.getUuid(), result.getFirst().roleUuid());
        verify(assignments).findByUserId(20L);
    }

    private UserRole assignment(long userId, long roleId, Instant revokedAt, Instant expiresAt) {
        UserRole assignment = new UserRole();
        assignment.setUserId(userId);
        assignment.setRoleId(roleId);
        assignment.setAssignedAt(Instant.now().minusSeconds(60));
        assignment.setRevokedAt(revokedAt);
        assignment.setExpiresAt(expiresAt);
        return assignment;
    }
}
