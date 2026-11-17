package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.LoginUserCommand;
import kh.edu.istad.platform.iam.domain.dto.result.AuthUserResult;
import kh.edu.istad.platform.iam.domain.port.out.AccessTokenProvider;
import kh.edu.istad.platform.iam.domain.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LoginAccountStatusTest {
    private final LoadUserForAuthenticationUseCase loadUser = mock(LoadUserForAuthenticationUseCase.class);
    private final PasswordHasher hasher = mock(PasswordHasher.class);
    private final AccessTokenProvider tokens = mock(AccessTokenProvider.class);
    private final UserSessionRepository sessions = mock(UserSessionRepository.class);

    @Test
    void lockedAccountCannotAuthenticate() {
        assertRejected(new AuthUserResult(UUID.randomUUID(), "user", null, "hash", true, true, List.of(), List.of()));
    }

    @Test
    void disabledAccountCannotAuthenticate() {
        assertRejected(new AuthUserResult(UUID.randomUUID(), "user", null, "hash", false, false, List.of(), List.of()));
    }

    @Test
    void deletedAccountIsRejectedBeforeAuthenticationAuthoritiesAreLoaded() {
        UserRepository users = mock(UserRepository.class);
        GetUserAuthoritiesUseCase authorities = mock(GetUserAuthoritiesUseCase.class);
        User user = mock(User.class);
        Username username = mock(Username.class);
        when(username.value()).thenReturn("user");
        when(user.getUsername()).thenReturn(username);
        when(user.isDeleted()).thenReturn(true);
        when(users.findByUsernameOrEmail("user")).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class,
                () -> new LoadUserForAuthenticationUseCase(users, authorities).execute("user"));
        verifyNoInteractions(authorities);
    }

    private void assertRejected(AuthUserResult user) {
        when(loadUser.execute("user")).thenReturn(user);
        when(hasher.matches("password", "hash")).thenReturn(true);
        LoginUserUseCase useCase = new LoginUserUseCase(loadUser, hasher, tokens, sessions);
        assertThrows(AccountUnavailableException.class,
                () -> useCase.execute(new LoginUserCommand("user", "password")));
        verifyNoInteractions(tokens, sessions);
    }
}
