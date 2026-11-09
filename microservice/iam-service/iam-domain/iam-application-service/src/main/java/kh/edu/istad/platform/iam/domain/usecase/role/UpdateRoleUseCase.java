package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.dto.command.UpdateRoleCommand;
import kh.edu.istad.platform.iam.domain.dto.result.RoleResult;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import org.springframework.stereotype.Component;

import java.time.Instant;
@Component
public class UpdateRoleUseCase {

    private final RoleRepository roleRepository;

    public UpdateRoleUseCase(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public RoleResult execute(UpdateRoleCommand command) {

        if (command == null || command.roleUuid() == null) {
            throw new IllegalArgumentException(
                    "Update role command and role UUID are required"
            );
        }

        Role role = roleRepository.findByUuid(command.roleUuid())
                .orElseThrow(() ->
                        new IllegalArgumentException("Role not found"));

        if (role.isDeleted()) {
            throw new IllegalStateException(
                    "Cannot update a deleted role"
            );
        }

        if (role.isSystem()) {
            throw new IllegalStateException(
                    "System roles cannot be modified"
            );
        }

        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }

        String newName = command.name().trim().toUpperCase();

        if (!role.getName().value().equals(newName)
                && roleRepository.existsByName(newName)) {
            throw new IllegalArgumentException("Role name already exists");
        }

        role.setName(new RoleName(newName));
        role.setDescription(command.description());
        role.setUpdatedAt(Instant.now());

        Role updatedRole = roleRepository.save(role);

        return new RoleResult(
                updatedRole.getUuid(),
                updatedRole.getName().value(),
                updatedRole.getDescription(),
                updatedRole.isSystem(),
                updatedRole.isActive(),
                updatedRole.getCreatedAt(),
                updatedRole.getUpdatedAt()
        );
    }
}
