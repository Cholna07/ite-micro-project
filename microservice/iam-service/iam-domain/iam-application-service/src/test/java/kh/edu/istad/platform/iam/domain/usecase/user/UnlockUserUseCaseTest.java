package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class UnlockUserUseCaseTest {
    @Test
    void unlocksResetsCounterAndRecordsActivityWithoutRestoringSessions() {
        UserRepository users = mock(UserRepository.class);
        CurrentActorProvider actor = mock(CurrentActorProvider.class);
        RecordAccountActivityEventUseCase activity = mock(RecordAccountActivityEventUseCase.class);
        UUID userUuid = UUID.randomUUID();
        User user = mock(User.class);
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(actor.getCurrentActor()).thenReturn("admin");

        new UnlockUserUseCase(users, actor, activity).execute(userUuid);

        verify(user).setAccountNonLocked(true);
        verify(user).setFailedLoginAttempts(0);
        verify(users).save(user);
        verify(activity).execute(userUuid, AccountActivityEventType.ACCOUNT_UNLOCKED,
                "Account unlocked; failed login counter reset");
        verifyNoMoreInteractions(activity);
    }
}
