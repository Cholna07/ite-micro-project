package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.GrantPermissionsToUserCommand;
import kh.edu.istad.platform.iam.domain.dto.result.PermissionGrantResult;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserPermission;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserPermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionEffect;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
@Component
public class GrantPermissionsToUserUseCase {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final CurrentActorProvider currentActorProvider;

    public GrantPermissionsToUserUseCase(
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

    public List<PermissionGrantResult> execute(
            GrantPermissionsToUserCommand command
    ) {

        if (command == null || command.userUuid() == null) {
            throw new IllegalArgumentException("Grant permission command and user UUID are required");
        }

        if (command.permissionUuids() == null || command.permissionUuids().isEmpty()) {
            throw new IllegalArgumentException("At least one permission is required");
        }

        if (new HashSet<>(command.permissionUuids()).size()
                != command.permissionUuids().size()) {
            throw new IllegalArgumentException("Duplicate permission UUIDs are not allowed");
        }

        User user = userRepository.findByUuid(command.userUuid())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isEnabled() || user.isDeleted()) {
            throw new IllegalStateException("Cannot grant permissions to an inactive user");
        }

        PermissionEffect effect;
        try {
            effect = PermissionEffect.valueOf(
                    command.effect().trim().toUpperCase(Locale.ROOT)
            );
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid permission effect", exception);
        }

        List<PermissionGrantResult> results = new ArrayList<>();

        for (UUID permissionUuid : command.permissionUuids()) {

            Permission permission = permissionRepository.findByUuid(permissionUuid)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Permission not found: " + permissionUuid
                    ));

            if (!permission.isActive() || permission.isDeleted()) {
                throw new IllegalStateException(
                        "Cannot assign an inactive permission: " + permissionUuid
                );
            }

            if (userPermissionRepository.existsActiveAssignment(
                    user.getId(), permission.getId())) {
                throw new IllegalStateException(
                        "User already has an active assignment for permission: "
                                + permissionUuid
                );
            }

            Instant assignedAt = Instant.now();

            UserPermission userPermission = new UserPermission();
            userPermission.setUserId(user.getId());
            userPermission.setPermissionId(permission.getId());
            userPermission.setEffect(effect);
            userPermission.setAssignedAt(assignedAt);
            userPermission.setAssignedBy(currentActorProvider.getCurrentActor());
            userPermission.setExpiresAt(command.expiresAt());
            userPermission.setReason(command.reason());

            userPermissionRepository.save(userPermission);

            results.add(new PermissionGrantResult(
                    user.getUuid(),
                    permission.getUuid(),
                    effect.name(),
                    assignedAt,
                    command.expiresAt()
            ));
        }

        return results;
    }
}
