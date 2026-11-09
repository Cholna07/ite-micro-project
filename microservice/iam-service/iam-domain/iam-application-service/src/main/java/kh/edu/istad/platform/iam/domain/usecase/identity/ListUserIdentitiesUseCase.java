package kh.edu.istad.platform.iam.domain.usecase.identity;

import kh.edu.istad.platform.iam.domain.dto.result.IdentityResult;
import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.iam.domain.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
@Component
public class ListUserIdentitiesUseCase {

    private final UserRepository userRepository;
    private final IdentityRepository identityRepository;

    public ListUserIdentitiesUseCase(
            UserRepository userRepository,
            IdentityRepository identityRepository
    ) {
        this.userRepository = userRepository;
        this.identityRepository = identityRepository;
    }

    public List<IdentityResult> execute(UUID userUuid) {

        if (userUuid == null) {
            throw new IllegalArgumentException(
                    "User UUID is required"
            );
        }

        var user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"
                ));

        if (user.isDeleted()) {
            throw new IllegalStateException(
                    "User has been deleted"
            );
        }

        return identityRepository.findByUserId(user.getId())
                .stream()
                .filter(identity -> !identity.isDeleted())
                .map(this::toResult)
                .toList();
    }

    private IdentityResult toResult(Identity identity) {

        return new IdentityResult(
                identity.getUuid(),
                identity.getProvider().name(),
                identity.getProviderSubject().value(),
                identity.getEmail().value(),
                identity.isEmailVerified(),
                identity.getDisplayName(),
                identity.getPictureUrl(),
                identity.getLinkedAt(),
                identity.getLastLoginAt()
        );
    }
}



