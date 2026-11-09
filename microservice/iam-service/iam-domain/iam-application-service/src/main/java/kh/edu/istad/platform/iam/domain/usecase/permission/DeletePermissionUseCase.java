package kh.edu.istad.platform.iam.domain.usecase.permission;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class DeletePermissionUseCase {

    private final PermissionRepository permissionRepository;
    private final CurrentActorProvider currentActorProvider;

    public DeletePermissionUseCase(
            PermissionRepository permissionRepository,
            CurrentActorProvider currentActorProvider
    ) {
        this.permissionRepository = permissionRepository;
        this.currentActorProvider = currentActorProvider;
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
                    "Permission has already been deleted"
            );
        }

        if (permission.isSystem()) {
            throw new IllegalStateException(
                    "System permission cannot be deleted"
            );
        }

        Instant now = Instant.now();

        permission.setDeleted(true);
        permission.setActive(false);
        permission.setDeletedAt(now);
        permission.setDeletedBy(currentActorProvider.getCurrentActor());
        permission.setUpdatedAt(now);

        permissionRepository.save(permission);
    }
}

