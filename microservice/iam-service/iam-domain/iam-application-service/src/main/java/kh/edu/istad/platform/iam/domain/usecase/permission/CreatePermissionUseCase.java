package kh.edu.istad.platform.iam.domain.usecase.permission;

import kh.edu.istad.platform.iam.domain.dto.command.CreatePermissionCommand;
import kh.edu.istad.platform.iam.domain.dto.result.PermissionResult;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.valueobject.Action;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionName;
import kh.edu.istad.platform.iam.domain.valueobject.Resource;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class CreatePermissionUseCase {

    private final PermissionRepository permissionRepository;

    public CreatePermissionUseCase(
            PermissionRepository permissionRepository
    ) {
        this.permissionRepository = permissionRepository;
    }

    public PermissionResult execute(CreatePermissionCommand command) {

        if (command == null) {
            throw new IllegalArgumentException(
                    "Permission command is required"
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

        String permissionName = command.name().trim().toLowerCase();
        String resource = command.resource().trim().toLowerCase();
        String action = command.action().trim().toLowerCase();

        if (permissionRepository.existsByName(permissionName)) {
            throw new IllegalArgumentException(
                    "Permission already exists"
            );
        }

        Instant now = Instant.now();

        Permission permission = new Permission();

        permission.setUuid(UUID.randomUUID());
        permission.setName(new PermissionName(permissionName));
        permission.setResource(new Resource(resource));
        permission.setAction(new Action(action));
        permission.setDescription(command.description());
        permission.setSystem(false);
        permission.setActive(true);
        permission.setDeleted(false);
        permission.setCreatedAt(now);

        Permission savedPermission = permissionRepository.save(permission);

        return new PermissionResult(
                savedPermission.getUuid(),
                savedPermission.getName().value(),
                savedPermission.getResource().value(),
                savedPermission.getAction().value(),
                savedPermission.getDescription(),
                savedPermission.isSystem(),
                savedPermission.isActive(),
                savedPermission.getCreatedAt(),
                savedPermission.getUpdatedAt()
        );
    }
}
