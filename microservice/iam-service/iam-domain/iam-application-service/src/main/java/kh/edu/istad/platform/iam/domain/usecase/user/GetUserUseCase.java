package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
public class GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResult execute(UUID userUuid) {

        // Find user by UUID
        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userUuid
                        )
                );

        // Convert User entity into UserResult
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
