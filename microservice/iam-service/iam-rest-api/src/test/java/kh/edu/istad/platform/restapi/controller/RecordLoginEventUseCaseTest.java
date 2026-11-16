package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.entity.LoginEvent;
import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.LoginEventRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordLoginAttemptUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordLoginEventUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordLoginEventUseCaseTest {
    @Mock UserRepository userRepository;
    @Mock LoginEventRepository loginEventRepository;
    @Mock RecordLoginAttemptUseCase recordLoginAttemptUseCase;

    @Test
    void persistsSuccessfulLoginWithoutAuthenticationSecrets() {
        UUID userUuid = UUID.randomUUID();
        User user = user(73L, userUuid);
        when(userRepository.findByUsernameOrEmail("alice")).thenReturn(Optional.of(user));
        RecordLoginEventUseCase useCase = new RecordLoginEventUseCase(
                userRepository, loginEventRepository, recordLoginAttemptUseCase);

        useCase.execute("alice", LoginEventOutcome.SUCCESS, "192.0.2.7", "browser", null);

        ArgumentCaptor<LoginEvent> event = ArgumentCaptor.forClass(LoginEvent.class);
        verify(loginEventRepository).save(event.capture());
        assertEquals(userUuid, event.getValue().userUuid());
        assertEquals(LoginEventOutcome.SUCCESS, event.getValue().outcome());
        assertNull(event.getValue().failureCategory());
        assertEquals("192.0.2.7", event.getValue().ipAddress());
        assertEquals("browser", event.getValue().userAgent());
        verify(recordLoginAttemptUseCase).execute("alice", true);
    }

    @Test
    void persistsFailedAttemptForUnknownAccountWithoutStoringSubmittedIdentifier() {
        when(userRepository.findByUsernameOrEmail("nobody")).thenReturn(Optional.empty());
        RecordLoginEventUseCase useCase = new RecordLoginEventUseCase(
                userRepository, loginEventRepository, recordLoginAttemptUseCase);

        useCase.execute("nobody", LoginEventOutcome.FAILURE, "192.0.2.8", "browser", "INVALID_CREDENTIALS");

        ArgumentCaptor<LoginEvent> event = ArgumentCaptor.forClass(LoginEvent.class);
        verify(loginEventRepository).save(event.capture());
        assertNull(event.getValue().userId());
        assertNull(event.getValue().userUuid());
        assertEquals(LoginEventOutcome.FAILURE, event.getValue().outcome());
        assertEquals("INVALID_CREDENTIALS", event.getValue().failureCategory());
        verifyNoInteractions(recordLoginAttemptUseCase);
    }

    private User user(Long id, UUID uuid) {
        User user = new User();
        user.setId(id);
        user.setUuid(uuid);
        return user;
    }
}
