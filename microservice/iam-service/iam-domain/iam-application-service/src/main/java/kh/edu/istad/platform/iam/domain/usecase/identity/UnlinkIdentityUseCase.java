package kh.edu.istad.platform.iam.domain.usecase.identity;

import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class UnlinkIdentityUseCase {

    private final UserRepository userRepository;
    private final IdentityRepository identityRepository;
    private final CurrentActorProvider currentActorProvider;

    public UnlinkIdentityUseCase(
            UserRepository userRepository,
            IdentityRepository identityRepository,
            CurrentActorProvider currentActorProvider
    ) {
        this.userRepository = userRepository;
        this.identityRepository = identityRepository;
        this.currentActorProvider = currentActorProvider;
    }

    public void execute(UUID userUuid, UUID identityUuid) {

        if (userUuid == null || identityUuid == null) {
            throw new IllegalArgumentException(
                    "User UUID and identity UUID are required"
            );
        }

        var user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"
                ));

        Identity identity = identityRepository.findByUuid(identityUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Identity not found"
                ));

        if (!identity.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "Identity does not belong to this user"
            );
        }

        if (identity.isDeleted()) {
            throw new IllegalStateException(
                    "Identity has already been unlinked"
            );
        }

        Instant now = Instant.now();

        identity.setDeleted(true);
        identity.setDeletedAt(now);
        identity.setDeletedBy(currentActorProvider.getCurrentActor());
        identity.setUpdatedAt(now);
        identity.setUpdatedBy(currentActorProvider.getCurrentActor());

        identityRepository.save(identity);
    }
}
