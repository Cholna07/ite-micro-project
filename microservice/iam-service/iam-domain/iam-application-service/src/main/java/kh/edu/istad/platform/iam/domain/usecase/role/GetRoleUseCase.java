package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.dto.result.RoleResult;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
public class GetRoleUseCase {

    private final RoleRepository roleRepository;

    public GetRoleUseCase(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public RoleResult execute(UUID roleUuid) {

        if (roleUuid == null) {
            throw new IllegalArgumentException("Role UUID is required");
        }

        Role role = roleRepository.findByUuid(roleUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("Role not found"));

        if (role.isDeleted()) {
            throw new IllegalStateException("Role has been deleted");
        }

        return new RoleResult(
                role.getUuid(),
                role.getName().value(),
                role.getDescription(),
                role.isSystem(),
                role.isActive(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
