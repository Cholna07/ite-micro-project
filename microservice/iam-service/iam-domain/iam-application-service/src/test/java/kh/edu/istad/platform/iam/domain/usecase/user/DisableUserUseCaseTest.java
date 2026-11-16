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

class DisableUserUseCaseTest {
    private final UserRepository users = mock(UserRepository.class);
    private final CurrentActorProvider actor = mock(CurrentActorProvider.class);
    private final UserSessionRepository sessions = mock(UserSessionRepository.class);
    private final RecordAccountActivityEventUseCase activity = mock(RecordAccountActivityEventUseCase.class);

    @Test
    void disablesUserRevokesAllCredentialTypesAndRecordsActivityForThatUser() {
        UUID targetId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        User target = mock(User.class);
        when(users.findByUuid(targetId)).thenReturn(Optional.of(target));
        when(actor.getCurrentActor()).thenReturn(actorId.toString());
        when(users.save(target)).thenReturn(target);

        new DisableUserUseCase(users, actor, sessions, activity).execute(targetId);

        verify(target).setEnabled(false);
        verify(users).save(target);
        verify(sessions).revokeAll(eq(targetId), eq(actorId), any(), eq("ACCOUNT_DEACTIVATED"));
        verify(activity).execute(targetId, AccountActivityEventType.ACCOUNT_DEACTIVATED,
                "Account deactivated; all active sessions were revoked");
    }

    @Test
    void doesNotRevokeAnyOtherUsersSessions() {
        UUID targetId = UUID.randomUUID();
        User target = mock(User.class);
        when(users.findByUuid(targetId)).thenReturn(Optional.of(target));
        when(actor.getCurrentActor()).thenReturn("SYSTEM");
        when(users.save(target)).thenReturn(target);

        new DisableUserUseCase(users, actor, sessions, activity).execute(targetId);

        verify(sessions).revokeAll(eq(targetId), isNull(), any(), eq("ACCOUNT_DEACTIVATED"));
        verifyNoMoreInteractions(sessions);
    }
}
