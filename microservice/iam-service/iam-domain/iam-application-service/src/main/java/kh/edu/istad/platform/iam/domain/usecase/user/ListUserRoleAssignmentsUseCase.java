package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.result.UserRoleAssignmentView;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRoleRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ListUserRoleAssignmentsUseCase {
    private final UserRepository users;
    private final UserRoleRepository assignments;
    private final RoleRepository roles;

    public ListUserRoleAssignmentsUseCase(UserRepository users, UserRoleRepository assignments, RoleRepository roles) {
        this.users = users;
        this.assignments = assignments;
        this.roles = roles;
    }

    public List<UserRoleAssignmentView> execute(UUID userUuid) {
        User user = users.findByUuid(userUuid).orElseThrow(() -> new IllegalArgumentException("User not found: " + userUuid));
        Instant now = Instant.now();
        Map<Long, Role> byId = roles.findAll().stream().collect(Collectors.toMap(Role::getId, Function.identity()));
        List<UserRoleAssignmentView> result = new ArrayList<>();
        assignments.findByUserId(user.getId()).stream()
                .filter(a -> a.getRevokedAt() == null && (a.getExpiresAt() == null || a.getExpiresAt().isAfter(now)))
                .forEach(assignment -> {
                    Role role = byId.get(assignment.getRoleId());
                    if (role != null && role.isActive() && !role.isDeleted()) {
                        result.add(new UserRoleAssignmentView(role.getUuid(), role.getName().value(), assignment.getAssignedAt(), assignment.getExpiresAt()));
                    }
                });
        return List.copyOf(result);
    }
}
