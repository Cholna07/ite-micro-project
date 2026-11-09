package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.result.UserAuthoritiesResult;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserPermission;
import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.iam.domain.port.out.*;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.RolePermission;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionEffect;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GetUserAuthoritiesUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserPermissionRepository userPermissionRepository;

    public GetUserAuthoritiesUseCase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            UserPermissionRepository userPermissionRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userPermissionRepository = userPermissionRepository;
    }

    public UserAuthoritiesResult execute(UUID userUuid) {

        if (userUuid == null) {
            throw new IllegalArgumentException("User UUID is required");
        }

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Instant now = Instant.now();

        // 1. Get active user-role assignments
        List<UserRole> userRoles =
                userRoleRepository.findActiveAssignmentsByUserId(user.getId());

        Set<Long> roleIds = userRoles.stream()
                .filter(assignment ->
                        assignment.getRevokedAt() == null)
                .filter(assignment ->
                        assignment.getExpiresAt() == null
                                || assignment.getExpiresAt().isAfter(now))
                .map(UserRole::getRoleId)
                .collect(Collectors.toSet());

        // 2. Get active roles
        List<Role> activeRoles = roleRepository.findAll()
                .stream()
                .filter(role -> roleIds.contains(role.getId()))
                .filter(role -> role.isActive() && !role.isDeleted())
                .toList();

        List<String> roles = activeRoles.stream()
                .map(role -> role.getName().value())
                .toList();

        // 3. Get active permissions
        List<Permission> activePermissions = permissionRepository.findAll()
                .stream()
                .filter(permission ->
                        permission.isActive() && !permission.isDeleted())
                .toList();

        Map<Long, Permission> permissionMap = activePermissions.stream()
                .collect(Collectors.toMap(
                        Permission::getId,
                        Function.identity()
                ));

        // 4. Get permissions inherited through roles
        Set<Long> grantedPermissionIds = new HashSet<>();

        for (Role role : activeRoles) {

            List<RolePermission> assignments =
                    rolePermissionRepository
                            .findActiveAssignmentsByRoleId(role.getId());

            for (RolePermission assignment : assignments) {

                boolean notRevoked = assignment.getRevokedAt() == null;

                boolean notExpired = assignment.getExpiresAt() == null
                        || assignment.getExpiresAt().isAfter(now);

                if (notRevoked && notExpired
                        && permissionMap.containsKey(assignment.getPermissionId())) {

                    grantedPermissionIds.add(assignment.getPermissionId());
                }
            }
        }

        // 5. Get directly assigned user permissions
        List<UserPermission> directAssignments =
                userPermissionRepository
                        .findActiveAssignmentsByUserId(user.getId());

        Set<Long> deniedPermissionIds = new HashSet<>();

        for (UserPermission assignment : directAssignments) {

            boolean notRevoked = assignment.getRevokedAt() == null;

            boolean notExpired = assignment.getExpiresAt() == null
                    || assignment.getExpiresAt().isAfter(now);

            if (!notRevoked || !notExpired) {
                continue;
            }

            Long permissionId = assignment.getPermissionId();

            if (!permissionMap.containsKey(permissionId)) {
                continue;
            }

            if (assignment.getEffect() == PermissionEffect.GRANT) {
                grantedPermissionIds.add(permissionId);

            } else if (assignment.getEffect() == PermissionEffect.DENY) {
                deniedPermissionIds.add(permissionId);
            }
        }

        // 6. DENY overrides both inherited and direct GRANT permissions
        grantedPermissionIds.removeAll(deniedPermissionIds);

        List<String> permissions = new ArrayList<>();

        for (Long permissionId : grantedPermissionIds) {

            Permission permission = permissionMap.get(permissionId);

            permissions.add(permission.getName().value());
        }

        return new UserAuthoritiesResult(
                user.getUuid(),
                roles,
                permissions
        );
    }
}