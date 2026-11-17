package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.domain.dto.command.AssignPermissionsToRoleCommand;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KokoAdminBootstrapRunnerTest {
    private static final UUID KOKO_UUID = UUID.fromString("5e64a765-5f52-4a02-a256-5d497e7f49e0");
    private static final List<String> ADMIN_PERMISSION_NAMES = List.of(
            "user.read", "user.manage", "user.role.assign", "user.permission.assign",
            "role.read", "role.manage", "role.permission.assign", "permission.read",
            "permission.manage", "session.read", "session.revoke", "client.create",
            "client.read", "client.manage", "activity.read", "audit.read",
            "system.config.read", "system.config.manage");

    @Mock UserRepository users;
    @Mock RoleRepository roles;
    @Mock PermissionRepository permissions;
    @Mock UserRoleRepository userRoles;
    @Mock RolePermissionRepository rolePermissions;
    @Mock CreateRoleUseCase createRole;
    @Mock CreatePermissionUseCase createPermission;
    @Mock AssignRolesToUserUseCase assignRoles;
    @Mock RevokeRoleFromUserUseCase revokeRole;
    @Mock AssignPermissionsToRoleUseCase assignPermissions;
    @Mock ActivateRoleUseCase activateRole;
    @Mock ActivatePermissionUseCase activatePermission;

    private User koko;
    private Role userRole;
    private Role adminRole;
    private List<Permission> adminPermissions;

    @BeforeEach
    void setUp() {
        koko = new User();
        koko.setId(4L);
        koko.setUuid(KOKO_UUID);
        koko.setUsername(new Username("koko"));
        koko.setEnabled(true);
        lenient().when(users.findByUsername("koko")).thenReturn(Optional.of(koko));

        userRole = role(20L, "USER");
        adminRole = role(21L, "ADMIN");
        lenient().when(roles.findByName("USER")).thenReturn(Optional.of(userRole));
        lenient().when(roles.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

        adminPermissions = new ArrayList<>();
        for (int i = 0; i < ADMIN_PERMISSION_NAMES.size(); i++) {
            String name = ADMIN_PERMISSION_NAMES.get(i);
            Permission permission = new Permission();
            permission.setId((long) (100 + i));
            permission.setUuid(UUID.randomUUID());
            permission.setActive(true);
            permission.setSystem(true);
            adminPermissions.add(permission);
            lenient().when(permissions.findByName(name)).thenReturn(Optional.of(permission));
        }
    }

    @Test
    void assignsOnlyAdminRoleAndAllEighteenAdminPermissionsToKoko() throws Exception {
        configureNoActiveAssignments();

        runner(true).run(mock(ApplicationArguments.class));

        ArgumentCaptor<AssignRolesToUserCommand> roleCommands = ArgumentCaptor.forClass(AssignRolesToUserCommand.class);
        verify(assignRoles).execute(roleCommands.capture());
        assertEquals(List.of(adminRole.getUuid()), roleCommands.getValue().roleUuids());
        assertEquals(KOKO_UUID, roleCommands.getValue().userUuid());
        assertEquals("One-time configured koko ADMIN bootstrap", roleCommands.getValue().reason());
        verifyNoInteractions(revokeRole);

        ArgumentCaptor<AssignPermissionsToRoleCommand> permissionCommand =
                ArgumentCaptor.forClass(AssignPermissionsToRoleCommand.class);
        verify(assignPermissions).execute(permissionCommand.capture());
        assertEquals(adminRole.getUuid(), permissionCommand.getValue().roleUuid());
        assertEquals(18, permissionCommand.getValue().permissionUuids().size());
        assertNotNull(KokoAdminBootstrapRunner.class.getMethod("run", ApplicationArguments.class)
                .getAnnotation(Transactional.class));
    }

    @Test
    void repeatedExecutionDoesNotCreateDuplicateActiveAssignments() {
        Set<Long> activeUserRoleIds = new HashSet<>();
        Set<String> activeRolePermissionPairs = new HashSet<>();
        when(userRoles.existsActiveAssignment(eq(4L), anyLong()))
                .thenAnswer(invocation -> activeUserRoleIds.contains(invocation.getArgument(1)));
        when(rolePermissions.existsActiveAssignment(anyLong(), anyLong()))
                .thenAnswer(invocation -> activeRolePermissionPairs.contains(pair(
                        invocation.getArgument(0), invocation.getArgument(1))));
        when(assignRoles.execute(any(AssignRolesToUserCommand.class))).thenAnswer(invocation -> {
            AssignRolesToUserCommand command = invocation.getArgument(0);
            command.roleUuids().forEach(uuid -> activeUserRoleIds.add(roleId(uuid)));
            return List.of();
        });
        when(assignPermissions.execute(any(AssignPermissionsToRoleCommand.class))).thenAnswer(invocation -> {
            AssignPermissionsToRoleCommand command = invocation.getArgument(0);
            command.permissionUuids().forEach(uuid -> activeRolePermissionPairs.add(pair(
                    adminRole.getId(), permissionId(uuid))));
            return List.of();
        });

        KokoAdminBootstrapRunner runner = runner(true);
        runner.run(mock(ApplicationArguments.class));
        runner.run(mock(ApplicationArguments.class));

        verify(assignRoles, times(1)).execute(any(AssignRolesToUserCommand.class));
        verify(assignPermissions, times(1)).execute(any(AssignPermissionsToRoleCommand.class));
    }

    @Test
    void missingAccountDoesNotCreateRolesOrAssignAnything() {
        when(users.findByUsername("koko")).thenReturn(Optional.empty());

        runner(true).run(mock(ApplicationArguments.class));

        verifyNoSetupOrAssignment();
    }

    @Test
    void disabledAccountDoesNotCreateRolesOrAssignAnything() {
        koko.setEnabled(false);

        runner(true).run(mock(ApplicationArguments.class));

        verifyNoSetupOrAssignment();
    }

    @Test
    void disabledFeatureDoesNotLookUpAccount() {
        runner(false).run(mock(ApplicationArguments.class));

        verifyNoInteractions(users, roles, permissions, userRoles, rolePermissions,
                createRole, createPermission, assignRoles, revokeRole, assignPermissions, activateRole, activatePermission);
    }

    private void configureNoActiveAssignments() {
        when(userRoles.existsActiveAssignment(eq(4L), anyLong())).thenReturn(false);
        when(userRoles.findActiveAssignment(eq(4L), eq(userRole.getId()))).thenReturn(Optional.empty());
        when(rolePermissions.existsActiveAssignment(anyLong(), anyLong())).thenReturn(false);
        when(assignRoles.execute(any(AssignRolesToUserCommand.class))).thenReturn(List.of());
        when(assignPermissions.execute(any(AssignPermissionsToRoleCommand.class))).thenReturn(List.of());
    }

    private KokoAdminBootstrapRunner runner(boolean enabled) {
        return new KokoAdminBootstrapRunner(enabled, users, roles, permissions, userRoles, rolePermissions,
                createRole, createPermission, assignRoles, revokeRole, assignPermissions, activateRole, activatePermission);
    }

    private void verifyNoSetupOrAssignment() {
        verifyNoInteractions(roles, permissions, userRoles, rolePermissions, createRole, createPermission,
                assignRoles, revokeRole, assignPermissions, activateRole, activatePermission);
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

    private Long roleId(UUID uuid) {
        return Map.of(userRole.getUuid(), userRole.getId(), adminRole.getUuid(), adminRole.getId()).get(uuid);
    }

    private Long permissionId(UUID uuid) {
        return adminPermissions.stream().filter(permission -> permission.getUuid().equals(uuid))
                .findFirst().orElseThrow().getId();
    }

    private String pair(Long roleId, Long permissionId) {
        return roleId + ":" + permissionId;
    }
}
