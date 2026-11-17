package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.port.out.AccountActivityRepository;
import kh.edu.istad.platform.iam.domain.port.out.RequestMetadataProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordAccountActivityEventUseCaseTest {
    @Test
    void recordsEveryCurrentlyDefinedActivityType() {
        AccountActivityRepository repository = mock(AccountActivityRepository.class);
        RequestMetadataProvider metadata = mock(RequestMetadataProvider.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var useCase = new RecordAccountActivityEventUseCase(repository, metadata);
        UUID userUuid = UUID.randomUUID();

        for (AccountActivityEventType type : AccountActivityEventType.values()) {
            useCase.execute(userUuid, type, "Security event");
        }

        verify(repository, times(AccountActivityEventType.values().length)).save(any());
    }
    @Mock AccountActivityRepository repository;
    @Mock RequestMetadataProvider metadata;

    @Test
    void persistsOnlyFixedEventFieldsAndSanitizesCredentialLikeUserAgentContent() {
        UUID userUuid = UUID.randomUUID();
        when(metadata.remoteAddress()).thenReturn("127.0.0.1");
        when(metadata.userAgent()).thenReturn("Browser Bearer abcdefghijklmnop access_token=secret-value");
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));

        var event = new RecordAccountActivityEventUseCase(repository, metadata).execute(userUuid,
                AccountActivityEventType.PASSWORD_CHANGED, "Password changed");

        assertEquals(userUuid, event.userUuid());
        assertEquals(AccountActivityEventType.PASSWORD_CHANGED, event.eventType());
        assertFalse(event.userAgent().contains("abcdefghijklmnop"));
        assertFalse(event.userAgent().contains("secret-value"));
        assertTrue(event.userAgent().contains("access_token=[redacted]"));
        assertNotNull(event.id());
        assertNotNull(event.occurredAt());
    }

    @Test
    void propagatesPersistenceFailuresInsteadOfClaimingTheEventWasRecorded() {
        when(metadata.remoteAddress()).thenReturn(null);
        when(metadata.userAgent()).thenReturn(null);
        when(repository.save(any())).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(kh.edu.istad.platform.iam.domain.exception.AccountActivityRecordingException.class,
                () -> new RecordAccountActivityEventUseCase(repository, metadata)
                .execute(UUID.randomUUID(), AccountActivityEventType.PROFILE_UPDATED, "Profile information updated"));
    }
}
