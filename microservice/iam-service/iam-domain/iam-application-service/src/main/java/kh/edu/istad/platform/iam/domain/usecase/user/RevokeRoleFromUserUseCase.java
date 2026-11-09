package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRoleRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class RevokeRoleFromUserUseCase {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final CurrentActorProvider currentActorProvider;

    public RevokeRoleFromUserUseCase(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            CurrentActorProvider currentActorProvider
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.currentActorProvider = currentActorProvider;
    }

    public void execute(UUID userUuid, UUID roleUuid, String reason) {

        if (userUuid == null || roleUuid == null) {
            throw new IllegalArgumentException(
                    "User UUID and role UUID cannot be null"
            );
        }

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + userUuid));

        // Find the active assignment using internal database IDs.
        UserRole assignment = userRoleRepository
                .findActiveAssignment(user.getId(), getRoleId(roleUuid))
                .orElseThrow(() ->
                        new IllegalArgumentException("Active role assignment not found"));

        assignment.setRevokedAt(Instant.now());
        assignment.setRevokedBy(currentActorProvider.getCurrentActor());
        assignment.setReason(reason);

        userRoleRepository.save(assignment);
    }

    private Long getRoleId(UUID roleUuid) {
        throw new UnsupportedOperationException(
                "Role lookup must be implemented using RoleRepository"
        );
    }
}
