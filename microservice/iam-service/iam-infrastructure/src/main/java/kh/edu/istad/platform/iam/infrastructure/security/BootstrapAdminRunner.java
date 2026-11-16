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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional first-admin bootstrap. Configure IAM_BOOTSTRAP_ADMIN_USERNAME to
 * grant the ADMIN role to an already registered, enabled account.
 */
@Component
public class BootstrapAdminRunner implements ApplicationRunner {
    private static final System.Logger LOG = System.getLogger(BootstrapAdminRunner.class.getName());
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
            new AdminPermission("activity.read", "activity", "read")
    );

    private final String bootstrapUsername;
    private final UserRepository users;
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final UserRoleRepository userRoles;
    private final RolePermissionRepository rolePermissions;
    private final CreateRoleUseCase createRole;
    private final CreatePermissionUseCase createPermission;
    private final AssignRolesToUserUseCase assignRoles;
    private final AssignPermissionsToRoleUseCase assignPermissions;
    private final ActivateRoleUseCase activateRole;
    private final ActivatePermissionUseCase activatePermission;

    public BootstrapAdminRunner(
            @Value("${iam.bootstrap-admin-username:}") String bootstrapUsername,
            UserRepository users,
            RoleRepository roles,
            PermissionRepository permissions,
            UserRoleRepository userRoles,
            RolePermissionRepository rolePermissions,
            CreateRoleUseCase createRole,
            CreatePermissionUseCase createPermission,
            AssignRolesToUserUseCase assignRoles,
            AssignPermissionsToRoleUseCase assignPermissions,
            ActivateRoleUseCase activateRole,
            ActivatePermissionUseCase activatePermission
    ) {
        this.bootstrapUsername = bootstrapUsername;
        this.users = users;
        this.roles = roles;
        this.permissions = permissions;
        this.userRoles = userRoles;
        this.rolePermissions = rolePermissions;
        this.createRole = createRole;
        this.createPermission = createPermission;
        this.assignRoles = assignRoles;
        this.assignPermissions = assignPermissions;
        this.activateRole = activateRole;
        this.activatePermission = activatePermission;
    }

    @Override
    public void run(ApplicationArguments args) {
        String username = bootstrapUsername == null ? "" : bootstrapUsername.trim();
        if (username.isEmpty()) return;

        User user = users.findByUsername(username).orElse(null);
        if (user == null) {
            LOG.log(System.Logger.Level.WARNING, "IAM bootstrap admin username was not found; no account was granted ADMIN.");
            return;
        }
        if (!user.isEnabled() || user.isDeleted()) {
            LOG.log(System.Logger.Level.WARNING, "IAM bootstrap admin account is disabled or deleted; no ADMIN role was assigned.");
            return;
        }

        Role admin = roles.findByName("ADMIN").orElseGet(() -> {
            var created = createRole.execute(new CreateRoleCommand("ADMIN", "System administrator role"));
            return roles.findByUuid(created.uuid()).orElseThrow();
        });
        if (admin.isDeleted()) {
            throw new IllegalStateException("The configured ADMIN role is deleted and cannot be bootstrapped.");
        }
        if (!admin.isActive() && !admin.isDeleted()) {
            activateRole.execute(admin.getUuid());
            admin = roles.findByUuid(admin.getUuid()).orElseThrow();
        }
        if (!admin.isSystem()) {
            admin.setSystem(true);
            admin = roles.save(admin);
        }

        List<Permission> adminPermissions = new ArrayList<>();
        for (AdminPermission spec : ADMIN_PERMISSIONS) {
            Permission permission = permissions.findByName(spec.name()).orElseGet(() -> {
                var created = createPermission.execute(new CreatePermissionCommand(
                        spec.name(), spec.resource(), spec.action(), "System administrator capability"
                ));
                return permissions.findByUuid(created.uuid()).orElseThrow();
            });
            if (permission.isDeleted()) {
                throw new IllegalStateException("A required system permission is deleted: " + spec.name());
            }
            if (!permission.isActive() && !permission.isDeleted()) {
                activatePermission.execute(permission.getUuid());
                permission = permissions.findByUuid(permission.getUuid()).orElseThrow();
            }
            if (!permission.isSystem()) {
                permission.setSystem(true);
                permission = permissions.save(permission);
            }
            adminPermissions.add(permission);
        }

        Long adminId = admin.getId();
        java.util.UUID adminUuid = admin.getUuid();
        List<java.util.UUID> missingPermissionUuids = adminPermissions.stream()
                .filter(permission -> !rolePermissions.existsActiveAssignment(adminId, permission.getId()))
                .map(Permission::getUuid)
                .toList();
        if (!missingPermissionUuids.isEmpty()) {
            assignPermissions.execute(new AssignPermissionsToRoleCommand(adminUuid, missingPermissionUuids, null, "Bootstrap administrator permissions"));
        }

        if (!userRoles.existsActiveAssignment(user.getId(), adminId)) {
            assignRoles.execute(new AssignRolesToUserCommand(user.getUuid(), List.of(adminUuid), null, "Configured bootstrap administrator"));
        }
        LOG.log(System.Logger.Level.INFO, "Configured the ADMIN role for the explicitly configured IAM bootstrap username.");
    }

    private record AdminPermission(String name, String resource, String action) {}
}
