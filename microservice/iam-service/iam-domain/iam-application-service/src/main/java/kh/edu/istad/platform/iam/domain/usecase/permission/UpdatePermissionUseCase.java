package kh.edu.istad.platform.iam.domain.usecase.permission;

import kh.edu.istad.platform.iam.domain.dto.command.UpdatePermissionCommand;
import kh.edu.istad.platform.iam.domain.dto.result.PermissionResult;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.valueobject.Action;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionName;
import kh.edu.istad.platform.iam.domain.valueobject.Resource;
import org.springframework.stereotype.Component;

import java.time.Instant;
@Component
public class UpdatePermissionUseCase {

    private final PermissionRepository permissionRepository;

    public UpdatePermissionUseCase(
            PermissionRepository permissionRepository
    ) {
        this.permissionRepository = permissionRepository;
    }

    public PermissionResult execute(UpdatePermissionCommand command) {

        if (command == null) {
            throw new IllegalArgumentException(
                    "Update permission command is required"
            );
        }

        if (command.permissionUuid() == null) {
            throw new IllegalArgumentException(
                    "Permission UUID is required"
            );
        }

        Permission permission = permissionRepository
                .findByUuid(command.permissionUuid())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Permission not found"
                ));

        if (permission.isDeleted()) {
            throw new IllegalStateException(
                    "Deleted permission cannot be updated"
            );
        }

        if (permission.isSystem()) {
            throw new IllegalStateException(
                    "System permission cannot be updated"
            );
        }

        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException(
                    "Permission name is required"
            );
        }

        if (command.resource() == null || command.resource().isBlank()) {
            throw new IllegalArgumentException(
                    "Permission resource is required"
            );
        }

        if (command.action() == null || command.action().isBlank()) {
            throw new IllegalArgumentException(
                    "Permission action is required"
            );
        }

        String name = command.name().trim().toLowerCase();
        String resource = command.resource().trim().toLowerCase();
        String action = command.action().trim().toLowerCase();

        boolean nameChanged = !permission.getName().value().equals(name);

        if (nameChanged && permissionRepository.existsByName(name)) {
            throw new IllegalArgumentException(
                    "Permission name already exists"
            );
        }

        permission.setName(new PermissionName(name));
        permission.setResource(new Resource(resource));
        permission.setAction(new Action(action));
        permission.setDescription(command.description());
        permission.setUpdatedAt(Instant.now());

        Permission updatedPermission = permissionRepository.save(permission);

        return new PermissionResult(
                updatedPermission.getUuid(),
                updatedPermission.getName().value(),
                updatedPermission.getResource().value(),
                updatedPermission.getAction().value(),
                updatedPermission.getDescription(),
                updatedPermission.isSystem(),
                updatedPermission.isActive(),
                updatedPermission.getCreatedAt(),
                updatedPermission.getUpdatedAt()
        );
    }
}
