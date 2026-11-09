package kh.edu.istad.platform.iam.domain.usecase.permission;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class DeactivatePermissionUseCase {

    private final PermissionRepository permissionRepository;

    public DeactivatePermissionUseCase(
            PermissionRepository permissionRepository
    ) {
        this.permissionRepository = permissionRepository;
    }

    public void execute(UUID permissionUuid) {

        if (permissionUuid == null) {
            throw new IllegalArgumentException(
                    "Permission UUID is required"
            );
        }

        Permission permission = permissionRepository
                .findByUuid(permissionUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Permission not found"
                ));

        if (permission.isDeleted()) {
            throw new IllegalStateException(
                    "Deleted permission cannot be deactivated"
            );
        }

        if (!permission.isActive()) {
            throw new IllegalStateException(
                    "Permission is already inactive"
            );
        }

        permission.setActive(false);
        permission.setUpdatedAt(Instant.now());

        permissionRepository.save(permission);
    }
}
