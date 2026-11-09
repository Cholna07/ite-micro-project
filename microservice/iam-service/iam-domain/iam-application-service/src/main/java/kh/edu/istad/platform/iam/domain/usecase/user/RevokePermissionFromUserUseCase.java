package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserPermission;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserPermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class RevokePermissionFromUserUseCase {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final CurrentActorProvider currentActorProvider;

    public RevokePermissionFromUserUseCase(
            UserRepository userRepository,
            PermissionRepository permissionRepository,
            UserPermissionRepository userPermissionRepository,
            CurrentActorProvider currentActorProvider
    ) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.currentActorProvider = currentActorProvider;
    }

    public void execute(UUID userUuid, UUID permissionUuid, String reason) {

        if (userUuid == null || permissionUuid == null) {
            throw new IllegalArgumentException(
                    "User UUID and Permission UUID are required"
            );
        }

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Permission permission = permissionRepository.findByUuid(permissionUuid)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));

        UserPermission assignment = userPermissionRepository
                .findActiveAssignment(user.getId(), permission.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active permission assignment not found"
                ));

        assignment.setRevokedAt(Instant.now());
        assignment.setRevokedBy(currentActorProvider.getCurrentActor());
        assignment.setReason(reason);

        userPermissionRepository.save(assignment);
    }
}
