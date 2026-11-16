package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.LoginUserCommand;
import kh.edu.istad.platform.iam.domain.dto.command.LoginSessionContext;
import kh.edu.istad.platform.iam.domain.dto.result.IssuedAccessToken;
import kh.edu.istad.platform.iam.domain.dto.result.AuthUserResult;
import kh.edu.istad.platform.iam.domain.dto.result.LoginResult;
import kh.edu.istad.platform.iam.domain.port.out.AccessTokenProvider;
import kh.edu.istad.platform.iam.domain.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.entity.SessionCredentialType;
import kh.edu.istad.platform.iam.domain.entity.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoginUserUseCase {

    private final LoadUserForAuthenticationUseCase loadUserForAuthenticationUseCase;
    private final PasswordHasher passwordHasher;
    private final AccessTokenProvider accessTokenProvider;
    private final UserSessionRepository userSessionRepository;

    public LoginResult execute(LoginUserCommand command, LoginSessionContext sessionContext) {

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

        UUID sessionId = UUID.randomUUID();
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        IssuedAccessToken issuedToken = accessTokenProvider.generateAccessToken(
                user.uuid(),
                user.username(),
                user.permissions(),
                sessionId,
                issuedAt
        );

        LoginSessionContext context = sessionContext == null
                ? new LoginSessionContext(null, null)
                : sessionContext;
        userSessionRepository.save(new UserSession(
                sessionId,
                user.uuid(),
                SessionCredentialType.JWT,
                "PASSWORD",
                null,
                issuedAt,
                issuedAt,
                issuedToken.expiresAt(),
                truncate(context.ipAddress(), 45),
                truncate(context.userAgent(), 512),
                null,
                null,
                null
        ));

        return new LoginResult(
                issuedToken.value(),
                "Bearer",
                user.uuid(),
                user.username(),
                user.authorities(),
                user.permissions()
        );
    }

    public LoginResult execute(LoginUserCommand command) {
        return execute(command, null);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value;
        return value.substring(0, maxLength);
    }
}
