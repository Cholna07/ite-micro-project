package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LockUserUseCaseTest {
    @Test
    void locksAccountRevokesAllSessionsAndRecordsActivity() {
        UserRepository users = mock(UserRepository.class);
        CurrentActorProvider actor = mock(CurrentActorProvider.class);
        UserSessionRepository sessions = mock(UserSessionRepository.class);
        RecordAccountActivityEventUseCase activity = mock(RecordAccountActivityEventUseCase.class);
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(users.findByUuid(userId)).thenReturn(Optional.of(user));
        when(actor.getCurrentActor()).thenReturn("admin");

        new LockUserUseCase(users, actor, sessions, activity).execute(userId);

        verify(user).setAccountNonLocked(false);
        verify(users).save(user);
        verify(sessions).revokeAll(eq(userId), isNull(), any(), eq("ACCOUNT_LOCKED"));
        verify(activity).execute(userId, AccountActivityEventType.ACCOUNT_LOCKED,
                "Account locked; all active sessions were revoked");
    }
}
