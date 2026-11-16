package kh.edu.istad.platform.iam.domain.usecase.permission;

import kh.edu.istad.platform.iam.domain.dto.command.CreatePermissionCommand;
import kh.edu.istad.platform.iam.domain.dto.result.PermissionResult;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.exception.PermissionNameConflictException;
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

        var definition = PermissionDefinitionValidator.validate(command.name(), command.resource(), command.action(), command.description());

        if (permissionRepository.existsByName(definition.name())) throw new PermissionNameConflictException();

        Instant now = Instant.now();

        Permission permission = new Permission();

        permission.setUuid(UUID.randomUUID());
        permission.setName(new PermissionName(definition.name()));
        permission.setResource(new Resource(definition.resource()));
        permission.setAction(new Action(definition.action()));
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
