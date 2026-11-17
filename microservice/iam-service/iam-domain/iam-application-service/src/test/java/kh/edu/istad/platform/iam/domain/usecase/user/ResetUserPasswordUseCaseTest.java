package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.ResetUserPasswordCommand;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.usecase.config.PasswordPolicy;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ResetUserPasswordUseCaseTest {
    @Test
    void hashesUpdatesTimestampRevokesSessionsAndRecordsActivity() {
        var users = mock(UserRepository.class);
        var hasher = mock(PasswordHasher.class);
        var actor = mock(CurrentActorProvider.class);
        var sessions = mock(UserSessionRepository.class);
        var activity = mock(RecordAccountActivityEventUseCase.class);
        var passwordPolicy = mock(PasswordPolicy.class);
        UUID uuid = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getUuid()).thenReturn(uuid);
        when(users.findByUuid(uuid)).thenReturn(Optional.of(user));
        when(actor.getCurrentActor()).thenReturn("admin");
        when(hasher.hash("new password")).thenReturn("bcrypt-hash");

        new ResetUserPasswordUseCase(users, hasher, actor, activity, sessions, passwordPolicy)
                .execute(new ResetUserPasswordCommand(uuid, "new password"));

        verify(user).setPasswordHash("bcrypt-hash");
        verify(user).setPasswordChangedAt(any());
        verify(users).save(user);
        verify(sessions).revokeAll(eq(uuid), isNull(), any(), eq("PASSWORD_RESET"));
        verify(activity).execute(uuid, AccountActivityEventType.PASSWORD_CHANGED,
                "Password reset by an administrator; all active sessions were revoked");
    }

    @Test
    void hashingFailureDoesNotPersistOrRevokeSessions() {
        var users = mock(UserRepository.class);
        var hasher = mock(PasswordHasher.class);
        var actor = mock(CurrentActorProvider.class);
        var sessions = mock(UserSessionRepository.class);
        var activity = mock(RecordAccountActivityEventUseCase.class);
        var passwordPolicy = mock(PasswordPolicy.class);
        UUID uuid = UUID.randomUUID();
        User user = mock(User.class);
        when(users.findByUuid(uuid)).thenReturn(Optional.of(user));
        when(hasher.hash("new password")).thenThrow(new IllegalStateException("hash failed"));

        assertThrows(IllegalStateException.class, () -> new ResetUserPasswordUseCase(users, hasher, actor, activity, sessions, passwordPolicy)
                .execute(new ResetUserPasswordCommand(uuid, "new password")));
        verify(users, never()).save(any());
        verifyNoInteractions(sessions, activity);
    }

    @Test
    void activityFailurePropagatesSoTransactionalCallerCanRollBackPasswordAndSessions() {
        var users = mock(UserRepository.class);
        var hasher = mock(PasswordHasher.class);
        var actor = mock(CurrentActorProvider.class);
        var sessions = mock(UserSessionRepository.class);
        var activity = mock(RecordAccountActivityEventUseCase.class);
        var passwordPolicy = mock(PasswordPolicy.class);
        UUID uuid = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getUuid()).thenReturn(uuid);
        when(users.findByUuid(uuid)).thenReturn(Optional.of(user));
        when(hasher.hash("new password")).thenReturn("bcrypt-hash");
        doThrow(new RuntimeException("activity failed")).when(activity)
                .execute(eq(uuid), eq(AccountActivityEventType.PASSWORD_CHANGED), anyString());

        assertThrows(RuntimeException.class, () -> new ResetUserPasswordUseCase(users, hasher, actor, activity, sessions, passwordPolicy)
                .execute(new ResetUserPasswordCommand(uuid, "new password")));
        verify(users).save(user);
        verify(sessions).revokeAll(eq(uuid), isNull(), any(), eq("PASSWORD_RESET"));
    }
}
