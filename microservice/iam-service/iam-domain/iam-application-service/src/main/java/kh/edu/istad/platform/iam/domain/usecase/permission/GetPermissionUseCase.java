package kh.edu.istad.platform.iam.domain.usecase.permission;

import kh.edu.istad.platform.iam.domain.dto.result.PermissionResult;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
public class GetPermissionUseCase {

    private final PermissionRepository permissionRepository;

    public GetPermissionUseCase(
            PermissionRepository permissionRepository
    ) {
        this.permissionRepository = permissionRepository;
    }

    public PermissionResult execute(UUID permissionUuid) {

        if (permissionUuid == null) {
            throw new IllegalArgumentException(
                    "Permission UUID is required"
            );
        }

        Permission permission = permissionRepository.findByUuid(permissionUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Permission not found"
                ));

        if (permission.isDeleted()) {
            throw new IllegalStateException(
                    "Permission has been deleted"
            );
        }

        return new PermissionResult(
                permission.getUuid(),
                permission.getName().value(),
                permission.getResource().value(),
                permission.getAction().value(),
                permission.getDescription(),
                permission.isSystem(),
                permission.isActive(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}
