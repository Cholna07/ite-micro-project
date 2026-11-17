package kh.edu.istad.platform.iam.domain.usecase.audit;

import kh.edu.istad.platform.iam.domain.dto.query.AuditLogFilter;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.AuditLogEvent;
import kh.edu.istad.platform.iam.domain.entity.AuditLogOutcome;
import kh.edu.istad.platform.iam.domain.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.RequestMetadataProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditLogUseCasesTest {
    private final AuditLogRepository repository = mock(AuditLogRepository.class);
    private final CurrentActorProvider actor = mock(CurrentActorProvider.class);
    private final UserRepository users = mock(UserRepository.class);
    private final RequestMetadataProvider metadata = mock(RequestMetadataProvider.class);

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive())
            TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.clear();
    }

    @Test
    void recordsAuthenticatedActorAndSafeMetadataWithoutRequestBodies() {
        UUID actorUuid = UUID.randomUUID();
        when(actor.getCurrentActor()).thenReturn(actorUuid.toString());
        when(metadata.remoteAddress()).thenReturn("192.0.2.10");
        when(metadata.userAgent()).thenReturn("Browser access_token=secret-value");

        new RecordAuditLogUseCase(repository, actor, users, metadata).record(
                "USER_PASSWORD_RESET", "USER", "target-user", AuditLogOutcome.SUCCESS, null);

        var captor = org.mockito.ArgumentCaptor.forClass(AuditLogEvent.class);
        verify(repository).append(captor.capture());
        AuditLogEvent event = captor.getValue();
        assertEquals(actorUuid, event.actorUuid());
        assertEquals("USER_PASSWORD_RESET", event.action());
        assertEquals("target-user", event.targetId());
        assertEquals(AuditLogOutcome.SUCCESS, event.outcome());
        assertEquals("192.0.2.10", event.ipAddress());
        assertFalse(event.userAgent().contains("secret-value"));
        assertNull(event.details());
    }

    @Test
    void failureEventsOnlyContainSanitizedFailureCategory() {
        when(actor.getCurrentActor()).thenReturn("SYSTEM");
        when(metadata.remoteAddress()).thenReturn(null);
        when(metadata.userAgent()).thenReturn(null);
        new RecordAuditLogUseCase(repository, actor, users, metadata).record(
                "ROLE_UPDATED", "ROLE", "id", AuditLogOutcome.FAILURE, "IllegalStateException: secret");
        var captor = org.mockito.ArgumentCaptor.forClass(AuditLogEvent.class);
        verify(repository).append(captor.capture());
        assertNull(captor.getValue().actorUuid());
        assertEquals("{\"failureCategory\":\"IllegalStateExceptionsecret\"}", captor.getValue().details());
    }

    @Test
    void successfulEventsArePersistedOnlyAfterCommitAndFailureEventsAfterRollback() {
        when(actor.getCurrentActor()).thenReturn(UUID.randomUUID().toString());
        when(metadata.remoteAddress()).thenReturn(null);
        when(metadata.userAgent()).thenReturn(null);
        RecordAuditLogUseCase recorder = new RecordAuditLogUseCase(repository, actor, users, metadata);

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        recorder.record("USER_LOCKED", "USER", "u-1", AuditLogOutcome.SUCCESS, null);
        verify(repository, never()).append(any());
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(repository, times(1)).append(any());
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.clear();

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        recorder.record("USER_LOCKED", "USER", "u-2", AuditLogOutcome.SUCCESS, null);
        TransactionSynchronizationManager.getSynchronizations().forEach(sync ->
                sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verify(repository, times(1)).append(any());
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.clear();

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        recorder.record("USER_LOCKED", "USER", "u-3", AuditLogOutcome.FAILURE, "IllegalStateException");
        TransactionSynchronizationManager.getSynchronizations().forEach(sync ->
                sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verify(repository, times(2)).append(any());
    }

    @Test
    void rejectsInvalidFiltersAndForwardsValidQueries() {
        ListAuditLogsUseCase useCase = new ListAuditLogsUseCase(repository);
        AuditLogFilter valid = new AuditLogFilter("USER", null, null, null, null, null,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-02-01T00:00:00Z"),
                0, 25, "occurredAt", "DESC");
        when(repository.find(valid)).thenReturn(new PageResult<>(List.of(), 0, 25, 0, 0, true, true));
        assertNotNull(useCase.execute(valid));
        verify(repository).find(valid);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new AuditLogFilter(
                null, null, null, null, null, null, null, null, 0, 101, "occurredAt", "DESC")));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new AuditLogFilter(
                null, null, null, null, null, null, valid.to(), valid.from(), 0, 25, "occurredAt", "DESC")));
        verify(repository, times(1)).find(any());
    }
}
