package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.AssignRolesToUserCommand;
import kh.edu.istad.platform.iam.domain.dto.result.RoleAssignmentResult;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRoleRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Component
public class AssignRolesToUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CurrentActorProvider currentActorProvider;

    public AssignRolesToUserUseCase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            CurrentActorProvider currentActorProvider
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.currentActorProvider = currentActorProvider;
    }

    public List<RoleAssignmentResult> execute(
            AssignRolesToUserCommand command
    ) {

        if (command == null) {
            throw new IllegalArgumentException(
                    "Assign roles command cannot be null"
            );
        }

        if (command.userUuid() == null) {
            throw new IllegalArgumentException(
                    "User UUID cannot be null"
            );
        }

        if (command.roleUuids() == null || command.roleUuids().isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one role must be provided"
            );
        }

        User user = userRepository.findByUuid(command.userUuid())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + command.userUuid()
                        ));

        if (user.isDeleted() || !user.isEnabled()) {
            throw new IllegalArgumentException(
                    "Cannot assign roles to a deleted or disabled user"
            );
        }

        Instant now = Instant.now();
        String actor = currentActorProvider.getCurrentActor();

        List<RoleAssignmentResult> results = new ArrayList<>();

        for (UUID roleUuid : command.roleUuids()) {

            if (roleUuid == null) {
                throw new IllegalArgumentException(
                        "Role UUID cannot be null"
                );
            }

            Role role = roleRepository.findByUuid(roleUuid)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Role not found: " + roleUuid
                            ));

            if (role.isDeleted() || !role.isActive()) {
                throw new IllegalArgumentException(
                        "Cannot assign an inactive or deleted role: " + roleUuid
                );
            }

            if (userRoleRepository.existsActiveAssignment(
                    user.getId(), role.getId())) {

                throw new IllegalArgumentException(
                        "User already has this role: " + roleUuid
                );
            }

            UserRole userRole = new UserRole();

            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRole.setAssignedAt(now);
            userRole.setAssignedBy(actor);
            userRole.setExpiresAt(command.expiresAt());
            userRole.setReason(command.reason());

            userRoleRepository.save(userRole);

            results.add(new RoleAssignmentResult(
                    user.getUuid(),
                    role.getUuid(),
                    now,
                    command.expiresAt()
            ));
        }

        return results;
    }
}