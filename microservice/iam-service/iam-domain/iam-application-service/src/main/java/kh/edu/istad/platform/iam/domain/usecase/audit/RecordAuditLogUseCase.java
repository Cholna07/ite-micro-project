package kh.edu.istad.platform.iam.domain.usecase.audit;

import kh.edu.istad.platform.iam.domain.entity.AuditLogEvent;
import kh.edu.istad.platform.iam.domain.entity.AuditLogOutcome;
import kh.edu.istad.platform.iam.domain.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.RequestMetadataProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class RecordAuditLogUseCase {
    private static final System.Logger LOG = System.getLogger(RecordAuditLogUseCase.class.getName());
    private static final Pattern BEARER = Pattern.compile("(?i)bearer\\s+\\S+");
    private static final Pattern CREDENTIAL = Pattern.compile("(?i)(access[_-]?token|refresh[_-]?token|password|secret|session[_-]?id)=\\S+");
    private static final Pattern JWT = Pattern.compile("[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}");

    private final AuditLogRepository repository;
    private final CurrentActorProvider actorProvider;
    private final UserRepository users;
    private final RequestMetadataProvider requestMetadata;

    public RecordAuditLogUseCase(AuditLogRepository repository, CurrentActorProvider actorProvider,
                                 UserRepository users, RequestMetadataProvider requestMetadata) {
        this.repository = repository;
        this.actorProvider = actorProvider;
        this.users = users;
        this.requestMetadata = requestMetadata;
    }

    public void record(String action, String targetType, String targetId, AuditLogOutcome outcome,
                       String failureCategory) {
        record(action, targetType, targetId, outcome, failureCategory, null);
    }

    public void record(String action, String targetType, String targetId, AuditLogOutcome outcome,
                       String failureCategory, String details) {
        if (action == null || action.isBlank() || targetType == null || targetType.isBlank()
                || outcome == null) return;
        String safeTargetId = limit(targetId, 200);
        if (safeTargetId == null) return;
        AuditLogEvent event = new AuditLogEvent(UUID.randomUUID(), resolveActorUuid(), limit(action, 80),
                limit(targetType, 40), safeTargetId, outcome, Instant.now(),
                limit(requestMetadata.remoteAddress(), 45), sanitizeUserAgent(requestMetadata.userAgent()),
                outcome == AuditLogOutcome.FAILURE ? failureDetails(failureCategory) : sanitizeDetails(details));
        persistAtOperationBoundary(event);
    }

    private UUID resolveActorUuid() {
        String actor = actorProvider.getCurrentActor();
        if (actor == null || actor.isBlank() || "SYSTEM".equals(actor)) return null;
        try {
            return UUID.fromString(actor);
        } catch (IllegalArgumentException ignored) {
            return users.findByUsernameOrEmail(actor).map(user -> user.getUuid()).orElse(null);
        }
    }

    private void persistAtOperationBoundary(AuditLogEvent event) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            persistSafely(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (event.outcome() == AuditLogOutcome.SUCCESS) persistSafely(event);
            }

            @Override
            public void afterCompletion(int status) {
                if (event.outcome() == AuditLogOutcome.FAILURE) persistSafely(event);
            }
        });
    }

    private void persistSafely(AuditLogEvent event) {
        try {
            repository.append(event);
        } catch (RuntimeException exception) {
            LOG.log(System.Logger.Level.ERROR, "Could not persist administrative audit event {0} ({1})",
                    event.action(), exception.getClass().getSimpleName());
        }
    }

    private String failureDetails(String category) {
        String safe = category == null ? "OperationFailure" : category.replaceAll("[^A-Za-z0-9_]", "");
        if (safe.isBlank()) safe = "OperationFailure";
        return "{\"failureCategory\":\"" + limit(safe, 80) + "\"}";
    }

    private String sanitizeDetails(String value) {
        if (value == null) return null;
        String safe = BEARER.matcher(value).replaceAll("Bearer [redacted]");
        safe = CREDENTIAL.matcher(safe).replaceAll("$1=[redacted]");
        safe = JWT.matcher(safe).replaceAll("[redacted]");
        return limit(safe, 2000);
    }

    private String sanitizeUserAgent(String value) {
        if (value == null) return null;
        String safe = BEARER.matcher(value).replaceAll("Bearer [redacted]");
        safe = CREDENTIAL.matcher(safe).replaceAll("$1=[redacted]");
        safe = JWT.matcher(safe).replaceAll("[redacted]");
        return limit(safe, 512);
    }

    private String limit(String value, int maxLength) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
}
