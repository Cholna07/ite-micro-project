package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.domain.dto.command.AssignPermissionsToRoleCommand;
import kh.edu.istad.platform.iam.domain.dto.command.AssignRolesToUserCommand;
import kh.edu.istad.platform.iam.domain.dto.command.CreatePermissionCommand;
import kh.edu.istad.platform.iam.domain.dto.command.CreateRoleCommand;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Explicit one-time bootstrap for the existing koko account. Enable with
 * {@code iam.bootstrap.koko-admin.enabled=true} for one application start,
 * then disable the property again.
 */
@Component
public class KokoAdminBootstrapRunner implements ApplicationRunner {
    private static final System.Logger LOG = System.getLogger(KokoAdminBootstrapRunner.class.getName());
    private static final String USERNAME = "koko";
    private static final List<AdminPermission> ADMIN_PERMISSIONS = List.of(
            new AdminPermission("user.read", "user", "read"),
            new AdminPermission("user.manage", "user", "manage"),
            new AdminPermission("user.role.assign", "user", "role.assign"),
            new AdminPermission("user.permission.assign", "user", "permission.assign"),
            new AdminPermission("role.read", "role", "read"),
            new AdminPermission("role.manage", "role", "manage"),
            new AdminPermission("role.permission.assign", "role", "permission.assign"),
            new AdminPermission("permission.read", "permission", "read"),
            new AdminPermission("permission.manage", "permission", "manage"),
            new AdminPermission("session.read", "session", "read"),
            new AdminPermission("session.revoke", "session", "revoke"),
            new AdminPermission("client.create", "client", "create"),
            new AdminPermission("client.read", "client", "read"),
            new AdminPermission("client.manage", "client", "manage"),
            new AdminPermission("activity.read", "activity", "read"),
            new AdminPermission("audit.read", "audit", "read"),
            new AdminPermission("system.config.read", "system.config", "read"),
            new AdminPermission("system.config.manage", "system.config", "manage")
    );

    private static final String ENABLED_PROPERTY = "iam.bootstrap.koko-admin.enabled";

    private final boolean enabled;
    private final UserRepository users;
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final UserRoleRepository userRoles;
    private final RolePermissionRepository rolePermissions;
    private final CreateRoleUseCase createRole;
    private final CreatePermissionUseCase createPermission;
    private final AssignRolesToUserUseCase assignRoles;
    private final RevokeRoleFromUserUseCase revokeRole;
    private final AssignPermissionsToRoleUseCase assignPermissions;
    private final ActivateRoleUseCase activateRole;
    private final ActivatePermissionUseCase activatePermission;

    public KokoAdminBootstrapRunner(
            @Value("${" + ENABLED_PROPERTY + ":false}") boolean enabled,
            UserRepository users,
            RoleRepository roles,
            PermissionRepository permissions,
            UserRoleRepository userRoles,
            RolePermissionRepository rolePermissions,
            CreateRoleUseCase createRole,
            CreatePermissionUseCase createPermission,
            AssignRolesToUserUseCase assignRoles,
            RevokeRoleFromUserUseCase revokeRole,
            AssignPermissionsToRoleUseCase assignPermissions,
            ActivateRoleUseCase activateRole,
            ActivatePermissionUseCase activatePermission
    ) {
        this.enabled = enabled;
        this.users = users;
        this.roles = roles;
        this.permissions = permissions;
        this.userRoles = userRoles;
        this.rolePermissions = rolePermissions;
        this.createRole = createRole;
        this.createPermission = createPermission;
        this.assignRoles = assignRoles;
        this.revokeRole = revokeRole;
        this.assignPermissions = assignPermissions;
        this.activateRole = activateRole;
        this.activatePermission = activatePermission;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) return;

        User user = users.findByUsername(USERNAME).orElse(null);
        if (user == null) {
            LOG.log(System.Logger.Level.WARNING, "Koko admin bootstrap enabled, but account koko was not found.");
            return;
        }
        if (!user.isEnabled() || user.isDeleted()) {
            LOG.log(System.Logger.Level.WARNING, "Koko admin bootstrap skipped because account koko is disabled or deleted.");
            return;
        }

        Role adminRole = ensureRole("ADMIN", "System administrator role");
        ensureAdminPermissions(adminRole);

        if (!userRoles.existsActiveAssignment(user.getId(), adminRole.getId())) {
            assignRoles.execute(new AssignRolesToUserCommand(user.getUuid(), List.of(adminRole.getUuid()), null,
                    "One-time configured koko ADMIN bootstrap"));
        }
        roles.findByName("USER").ifPresent(userRole -> userRoles.findActiveAssignment(user.getId(), userRole.getId())
                .ifPresent(assignment -> revokeRole.execute(user.getUuid(), userRole.getUuid(),
                        "Configured bootstrap administrator uses the ADMIN role exclusively")));
        LOG.log(System.Logger.Level.INFO, "One-time koko ADMIN-only bootstrap completed.");
    }

    private Role ensureRole(String name, String description) {
        Role role = roles.findByName(name).orElseGet(() -> {
            var created = createRole.execute(new CreateRoleCommand(name, description));
            return roles.findByUuid(created.uuid()).orElseThrow();
        });
        if (role.isDeleted()) {
            throw new IllegalStateException("The required " + name + " role is deleted and cannot be bootstrapped.");
        }
        if (!role.isActive()) {
            activateRole.execute(role.getUuid());
            role = roles.findByUuid(role.getUuid()).orElseThrow();
        }
        if (!role.isSystem()) {
            role.setSystem(true);
            role = roles.save(role);
        }
        return role;
    }

    private void ensureAdminPermissions(Role adminRole) {
        List<Permission> requiredPermissions = new ArrayList<>(ADMIN_PERMISSIONS.size());
        for (AdminPermission spec : ADMIN_PERMISSIONS) {
            Permission permission = permissions.findByName(spec.name()).orElseGet(() -> {
                var created = createPermission.execute(new CreatePermissionCommand(
                        spec.name(), spec.resource(), spec.action(), "System administrator capability"));
                return permissions.findByUuid(created.uuid()).orElseThrow();
            });
            if (permission.isDeleted()) {
                throw new IllegalStateException("A required system permission is deleted: " + spec.name());
            }
            if (!permission.isActive()) {
                activatePermission.execute(permission.getUuid());
                permission = permissions.findByUuid(permission.getUuid()).orElseThrow();
            }
            if (!permission.isSystem()) {
                permission.setSystem(true);
                permission = permissions.save(permission);
            }
            requiredPermissions.add(permission);
        }

        List<UUID> missingPermissionUuids = requiredPermissions.stream()
                .filter(permission -> !rolePermissions.existsActiveAssignment(adminRole.getId(), permission.getId()))
                .map(Permission::getUuid)
                .toList();
        if (!missingPermissionUuids.isEmpty()) {
            assignPermissions.execute(new AssignPermissionsToRoleCommand(adminRole.getUuid(), missingPermissionUuids,
                    null, "One-time configured koko ADMIN permissions"));
        }
    }

    private record AdminPermission(String name, String resource, String action) {}
}
