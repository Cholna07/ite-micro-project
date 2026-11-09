package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.result.AuthUserResult;
import kh.edu.istad.platform.iam.domain.dto.result.UserAuthoritiesResult;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class LoadUserForAuthenticationUseCase {

    private final UserRepository userRepository;
    private final GetUserAuthoritiesUseCase getUserAuthoritiesUseCase;

    public LoadUserForAuthenticationUseCase(
            UserRepository userRepository,
            GetUserAuthoritiesUseCase getUserAuthoritiesUseCase
    ) {
        this.userRepository = userRepository;
        this.getUserAuthoritiesUseCase = getUserAuthoritiesUseCase;
    }

    public AuthUserResult execute(String usernameOrEmail) {

        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Username or email is required"
            );
        }

        User user = userRepository
                .findByUsernameOrEmail(usernameOrEmail.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (user.isDeleted()) {
            throw new IllegalStateException("User account has been deleted");
        }

        UserAuthoritiesResult authorities =
                getUserAuthoritiesUseCase.execute(user.getUuid());

        return new AuthUserResult(
                user.getUuid(),
                user.getUsername().value(),
                user.getEmail().value(),
                user.getPasswordHash(),
                user.isEnabled(),
                !user.isAccountNonLocked(),
                authorities.roles().stream()
                        .map(role -> "ROLE_" + role)
                        .toList(),
                authorities.permissions()
        );
    }
}