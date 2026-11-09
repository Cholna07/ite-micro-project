package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.dto.command.CreateRoleCommand;
import kh.edu.istad.platform.iam.domain.dto.result.RoleResult;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class CreateRoleUseCase {

    private final RoleRepository roleRepository;

    public CreateRoleUseCase(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public RoleResult execute(CreateRoleCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("Role command is required");
        }

        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }

        String roleName = command.name().trim().toUpperCase();

        if (roleRepository.existsByName(roleName)) {
            throw new IllegalArgumentException("Role already exists");
        }

        Instant now = Instant.now();

        Role role = new Role();
        role.setUuid(UUID.randomUUID());
        role.setName(new RoleName(roleName));
        role.setDescription(command.description());
        role.setSystem(false);
        role.setActive(true);
        role.setDeleted(false);
        role.setCreatedAt(now);

        Role savedRole = roleRepository.save(role);

        return new RoleResult(
                savedRole.getUuid(),
                savedRole.getName().value(),
                savedRole.getDescription(),
                savedRole.isSystem(),
                savedRole.isActive(),
                savedRole.getCreatedAt(),
                savedRole.getUpdatedAt()
        );
    }
}
