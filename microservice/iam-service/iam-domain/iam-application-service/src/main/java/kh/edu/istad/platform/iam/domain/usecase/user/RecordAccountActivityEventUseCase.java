package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.AccountActivityEvent;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.exception.AccountActivityRecordingException;
import kh.edu.istad.platform.iam.domain.port.out.AccountActivityRepository;
import kh.edu.istad.platform.iam.domain.port.out.RequestMetadataProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class RecordAccountActivityEventUseCase {
    private static final Pattern BEARER = Pattern.compile("(?i)bearer\\s+\\S+");
    private static final Pattern CREDENTIAL_PARAMETER = Pattern.compile("(?i)(access[_-]?token|refresh[_-]?token|password|secret|session[_-]?id)=\\S+");
    private static final Pattern JWT = Pattern.compile("[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}");

    private final AccountActivityRepository repository;
    private final RequestMetadataProvider requestMetadata;

    public RecordAccountActivityEventUseCase(AccountActivityRepository repository,
                                             RequestMetadataProvider requestMetadata) {
        this.repository = repository;
        this.requestMetadata = requestMetadata;
    }

    public AccountActivityEvent execute(UUID userUuid, AccountActivityEventType type, String description) {
        if (userUuid == null || type == null || description == null || description.isBlank()) {
            throw new IllegalArgumentException("Account activity user, event type, and description are required");
        }
        String safeDescription = description.trim();
        if (safeDescription.length() > 500) safeDescription = safeDescription.substring(0, 500);
        String ipAddress = limit(requestMetadata.remoteAddress(), 45);
        String userAgent = sanitizeUserAgent(requestMetadata.userAgent());
        try {
            return repository.save(new AccountActivityEvent(UUID.randomUUID(), userUuid, type, safeDescription,
                    Instant.now(), ipAddress, userAgent));
        } catch (RuntimeException exception) {
            throw new AccountActivityRecordingException(exception);
        }
    }

    private String sanitizeUserAgent(String value) {
        if (value == null) return null;
        String safe = BEARER.matcher(value).replaceAll("Bearer [redacted]");
        safe = CREDENTIAL_PARAMETER.matcher(safe).replaceAll("$1=[redacted]");
        safe = JWT.matcher(safe).replaceAll("[redacted]");
        return limit(safe, 512);
    }

    private String limit(String value, int maxLength) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
}
