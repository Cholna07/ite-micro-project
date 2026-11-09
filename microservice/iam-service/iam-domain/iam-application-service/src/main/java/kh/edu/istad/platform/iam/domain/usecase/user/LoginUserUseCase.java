package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.LoginUserCommand;
import kh.edu.istad.platform.iam.domain.dto.result.AuthUserResult;
import kh.edu.istad.platform.iam.domain.dto.result.LoginResult;
import kh.edu.istad.platform.iam.domain.port.out.AccessTokenProvider;
import kh.edu.istad.platform.iam.domain.port.out.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginUserUseCase {

    private final LoadUserForAuthenticationUseCase loadUserForAuthenticationUseCase;
    private final PasswordHasher passwordHasher;
    private final AccessTokenProvider accessTokenProvider;

    public LoginResult execute(LoginUserCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("Login information is required");
        }

        if (command.usernameOrEmail() == null
                || command.usernameOrEmail().isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }

        if (command.password() == null || command.password().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        AuthUserResult user = loadUserForAuthenticationUseCase.execute(
                command.usernameOrEmail()
        );

        if (!passwordHasher.matches(
                command.password(),
                user.passwordHash()
        )) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }

        if (!user.enabled()) {
            throw new IllegalStateException("User account is disabled");
        }

        if (user.accountLocked()) {
            throw new IllegalStateException("User account is locked");
        }

        String accessToken = accessTokenProvider.generateAccessToken(
                user.uuid(),
                user.username(),
                user.permissions()
        );

        return new LoginResult(
                accessToken,
                "Bearer",
                user.uuid(),
                user.username(),
                user.authorities(),
                user.permissions()
        );
    }
}