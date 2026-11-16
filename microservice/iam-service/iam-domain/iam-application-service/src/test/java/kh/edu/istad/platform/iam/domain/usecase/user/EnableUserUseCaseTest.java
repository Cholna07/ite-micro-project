package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class EnableUserUseCaseTest {
    @Test
    void reactivationOnlyEnablesAccountAndDoesNotRestoreRevokedSessions() {
        UserRepository users = mock(UserRepository.class);
        CurrentActorProvider actor = mock(CurrentActorProvider.class);
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(users.findByUuid(userId)).thenReturn(Optional.of(user));
        when(actor.getCurrentActor()).thenReturn("admin");

        new EnableUserUseCase(users, actor).execute(userId);

        verify(user).setEnabled(true);
        verify(users).save(user);
    }
}
