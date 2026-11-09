package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class DeleteRoleUseCase {

    private final RoleRepository roleRepository;
    private final CurrentActorProvider currentActorProvider;

    public DeleteRoleUseCase(
            RoleRepository roleRepository,
            CurrentActorProvider currentActorProvider
    ) {
        this.roleRepository = roleRepository;
        this.currentActorProvider = currentActorProvider;
    }

    public void execute(UUID roleUuid) {

        if (roleUuid == null) {
            throw new IllegalArgumentException("Role UUID is required");
        }

        Role role = roleRepository.findByUuid(roleUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("Role not found"));

        if (role.isDeleted()) {
            throw new IllegalStateException("Role is already deleted");
        }

        if (role.isSystem()) {
            throw new IllegalStateException(
                    "System roles cannot be deleted"
            );
        }

        Instant now = Instant.now();

        role.setDeleted(true);
        role.setActive(false);
        role.setDeletedAt(now);
        role.setDeletedBy(currentActorProvider.getCurrentActor());
        role.setUpdatedAt(now);

        roleRepository.save(role);
    }
}
