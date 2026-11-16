package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.result.UserPermissionAssignmentView;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserPermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ListUserPermissionAssignmentsUseCase {
    private final UserRepository users;
    private final UserPermissionRepository assignments;
    private final PermissionRepository permissions;

    public ListUserPermissionAssignmentsUseCase(UserRepository users, UserPermissionRepository assignments, PermissionRepository permissions) {
        this.users = users;
        this.assignments = assignments;
        this.permissions = permissions;
    }

    public List<UserPermissionAssignmentView> execute(UUID userUuid) {
        User user = users.findByUuid(userUuid).orElseThrow(() -> new IllegalArgumentException("User not found: " + userUuid));
        Instant now = Instant.now();
        Map<Long, Permission> byId = permissions.findAll().stream().collect(Collectors.toMap(Permission::getId, Function.identity()));
        List<UserPermissionAssignmentView> result = new ArrayList<>();
        assignments.findByUserId(user.getId()).stream()
                .filter(a -> a.getRevokedAt() == null && (a.getExpiresAt() == null || a.getExpiresAt().isAfter(now)))
                .forEach(assignment -> {
                    Permission permission = byId.get(assignment.getPermissionId());
                    if (permission != null && permission.isActive() && !permission.isDeleted()) {
                        result.add(new UserPermissionAssignmentView(permission.getUuid(), permission.getName().value(), assignment.getEffect().name(), assignment.getAssignedAt(), assignment.getExpiresAt()));
                    }
                });
        return List.copyOf(result);
    }
}
