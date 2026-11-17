package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.domain.dto.command.ChangePasswordCommand;
import kh.edu.istad.platform.iam.domain.dto.command.LoginUserCommand;
import kh.edu.istad.platform.iam.domain.dto.result.AuthUserResult;
import kh.edu.istad.platform.iam.domain.dto.result.IssuedAccessToken;
import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.AccessTokenProvider;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.SystemConfigurationRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.usecase.config.PasswordPolicy;
import kh.edu.istad.platform.iam.domain.usecase.user.ChangePasswordUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.LoadUserForAuthenticationUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.LoginUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangePasswordLoginFlowTest {
    @Test
    void changedBcryptPasswordAuthenticatesOnTheNextLogin() {
        var users = mock(UserRepository.class);
        var currentActor = mock(CurrentActorProvider.class);
        var activity = mock(RecordAccountActivityEventUseCase.class);
        var config = mock(SystemConfigurationRepository.class);
        var loadForLogin = mock(LoadUserForAuthenticationUseCase.class);
        var tokenProvider = mock(AccessTokenProvider.class);
        var sessions = mock(UserSessionRepository.class);
        when(config.findCurrent()).thenReturn(IamSystemConfiguration.DEFAULTS);

        UUID uuid = UUID.randomUUID();
        User user = new User();
        user.setUuid(uuid);
        user.setUsername(new Username("owner"));
        user.setPasswordHash(new BCryptPasswordHasher().hash("current-password"));
        when(users.findByUuid(uuid)).thenReturn(Optional.of(user));
        when(currentActor.getCurrentActor()).thenReturn(uuid.toString());
        when(users.save(user)).thenReturn(user);

        BCryptPasswordHasher passwordHasher = new BCryptPasswordHasher();
        new ChangePasswordUseCase(users, passwordHasher, currentActor, activity, new PasswordPolicy(config))
                .execute(new ChangePasswordCommand(uuid, "current-password", "new-password-123"));

        assertNotEquals("new-password-123", user.getPasswordHash());
        assertTrue(passwordHasher.matches("new-password-123", user.getPasswordHash()));
        assertFalse(passwordHasher.matches("current-password", user.getPasswordHash()));

        when(loadForLogin.execute("owner")).thenReturn(new AuthUserResult(uuid, "owner", null,
                user.getPasswordHash(), true, false, List.of(), List.of()));
        when(tokenProvider.generateAccessToken(eq(uuid), eq("owner"), any(), any(), any()))
                .thenReturn(new IssuedAccessToken("test-token", Instant.now().plusSeconds(600)));
        LoginUserUseCase login = new LoginUserUseCase(loadForLogin, passwordHasher, tokenProvider, sessions);
        assertEquals(uuid, login.execute(new LoginUserCommand("owner", "new-password-123")).userUuid());
        assertThrows(IllegalArgumentException.class,
                () -> login.execute(new LoginUserCommand("owner", "current-password")));
        verify(sessions, times(1)).save(any());
    }
}
