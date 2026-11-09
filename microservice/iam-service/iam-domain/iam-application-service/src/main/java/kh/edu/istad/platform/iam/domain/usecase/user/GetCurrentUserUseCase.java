
package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;
    private final CurrentActorProvider currentActorProvider;

    public UserResult execute() {

        String currentActor = currentActorProvider.getCurrentActor();

        if (currentActor == null || currentActor.isBlank()
                || currentActor.equals("SYSTEM")) {
            throw new IllegalStateException("Current user is not authenticated");
        }

        UUID userUuid;

        try {
            userUuid = UUID.fromString(currentActor);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Invalid authenticated user identifier");
        }

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("Current user not found")
                );

        if (user.isDeleted()) {
            throw new IllegalStateException("User account has been deleted");
        }

        return new UserResult(
                user.getUuid(),
                user.getUsername().value(),
                user.getEmail().value(),
                user.getGivenName(),
                user.getFamilyName(),
                user.getMiddleName(),
                user.getNickname(),
                user.getGender(),
                user.getPhoneNumber() == null
                        ? null
                        : user.getPhoneNumber().value(),
                user.isEmailVerified(),
                user.isPhoneNumberVerified(),
                user.isEnabled(),
                !user.isAccountNonLocked(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
