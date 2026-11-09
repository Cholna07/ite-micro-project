package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class ActivateRoleUseCase {

    private final RoleRepository roleRepository;

    public ActivateRoleUseCase(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public void execute(UUID roleUuid) {

        if (roleUuid == null) {
            throw new IllegalArgumentException("Role UUID is required");
        }

        Role role = roleRepository.findByUuid(roleUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("Role not found"));

        if (role.isDeleted()) {
            throw new IllegalStateException(
                    "Cannot activate a deleted role"
            );
        }

        if (role.isActive()) {
            throw new IllegalStateException(
                    "Role is already active"
            );
        }

        role.setActive(true);
        role.setUpdatedAt(Instant.now());

        roleRepository.save(role);
    }
}
