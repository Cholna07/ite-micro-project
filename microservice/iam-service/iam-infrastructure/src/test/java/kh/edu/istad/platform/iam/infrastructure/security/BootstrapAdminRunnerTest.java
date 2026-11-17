package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.domain.dto.command.AssignRolesToUserCommand;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RolePermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRoleRepository;
import kh.edu.istad.platform.iam.domain.usecase.permission.ActivatePermissionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.permission.CreatePermissionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.ActivateRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.AssignPermissionsToRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.CreateRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.AssignRolesToUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RevokeRoleFromUserUseCase;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BootstrapAdminRunnerTest {
    private static final UUID BOOTSTRAP_USER_UUID = UUID.fromString("5e64a765-5f52-4a02-a256-5d497e7f49e0");

    @Test
    void assignsOnlyAdminAcrossRepeatedRuns() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        PermissionRepository permissions = mock(PermissionRepository.class);
        UserRoleRepository userRoles = mock(UserRoleRepository.class);
        RolePermissionRepository rolePermissions = mock(RolePermissionRepository.class);
        CreateRoleUseCase createRole = mock(CreateRoleUseCase.class);
        CreatePermissionUseCase createPermission = mock(CreatePermissionUseCase.class);
        AssignRolesToUserUseCase assignRoles = mock(AssignRolesToUserUseCase.class);
        RevokeRoleFromUserUseCase revokeRole = mock(RevokeRoleFromUserUseCase.class);
        AssignPermissionsToRoleUseCase assignPermissions = mock(AssignPermissionsToRoleUseCase.class);
        ActivateRoleUseCase activateRole = mock(ActivateRoleUseCase.class);
        ActivatePermissionUseCase activatePermission = mock(ActivatePermissionUseCase.class);

        User koko = new User();
        koko.setId(4L);
        koko.setUuid(BOOTSTRAP_USER_UUID);
        koko.setUsername(new Username("koko"));
        koko.setEnabled(true);
        when(users.findByUsernameOrEmail("koko")).thenReturn(Optional.of(koko));

        Role userRole = role(10L, "USER");
        Role adminRole = role(11L, "ADMIN");
        when(roles.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roles.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

        Permission existingPermission = new Permission();
        existingPermission.setId(100L);
        existingPermission.setUuid(UUID.randomUUID());
        existingPermission.setActive(true);
        existingPermission.setSystem(true);
        when(permissions.findByName(anyString())).thenReturn(Optional.of(existingPermission));
        when(rolePermissions.existsActiveAssignment(11L, 100L)).thenReturn(true);

        Set<Long> assignedRoleIds = new HashSet<>();
        when(userRoles.existsActiveAssignment(eq(4L), anyLong()))
                .thenAnswer(invocation -> assignedRoleIds.contains(invocation.getArgument(1)));
        when(userRoles.findActiveAssignment(eq(4L), anyLong())).thenReturn(Optional.empty());
        when(assignRoles.execute(any(AssignRolesToUserCommand.class))).thenAnswer(invocation -> {
            AssignRolesToUserCommand command = invocation.getArgument(0);
            command.roleUuids().forEach(uuid -> assignedRoleIds.add(
                    Map.of(userRole.getUuid(), userRole.getId(), adminRole.getUuid(), adminRole.getId()).get(uuid)));
            return List.of();
        });

        BootstrapAdminRunner runner = new BootstrapAdminRunner("koko", users, roles, permissions, userRoles,
                rolePermissions, createRole, createPermission, assignRoles, revokeRole, assignPermissions, activateRole,
                activatePermission);
        ApplicationArguments args = mock(ApplicationArguments.class);

        runner.run(args);
        runner.run(args);

        verify(assignRoles, times(1)).execute(any(AssignRolesToUserCommand.class));
        verify(assignRoles).execute(argThat(command -> command.userUuid().equals(BOOTSTRAP_USER_UUID)
                && command.roleUuids().equals(List.of(adminRole.getUuid()))
                && command.reason().equals("Configured bootstrap administrator")));
        verify(revokeRole, never()).execute(any(), any(), anyString());
        verify(createRole, never()).execute(any());
        verify(activateRole, never()).execute(any());
        verify(assignPermissions, never()).execute(any());
    }

    private Role role(long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setUuid(UUID.randomUUID());
        role.setName(new RoleName(name));
        role.setActive(true);
        role.setSystem(true);
        return role;
    }
}
