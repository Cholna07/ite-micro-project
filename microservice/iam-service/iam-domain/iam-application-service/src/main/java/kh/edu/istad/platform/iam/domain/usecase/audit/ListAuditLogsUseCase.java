package kh.edu.istad.platform.iam.domain.usecase.audit;

import kh.edu.istad.platform.iam.domain.dto.query.AuditLogFilter;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.AuditLogEvent;
import kh.edu.istad.platform.iam.domain.port.out.AuditLogRepository;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ListAuditLogsUseCase {
    private static final Set<String> SORT_FIELDS = Set.of("occurredAt", "action", "targetType", "targetId", "outcome", "actorUuid");
    private final AuditLogRepository repository;

    public ListAuditLogsUseCase(AuditLogRepository repository) { this.repository = repository; }

    public PageResult<AuditLogEvent> execute(AuditLogFilter filter) {
        if (filter == null) throw new IllegalArgumentException("Audit log filter is required");
        if (filter.page() < 0 || filter.size() < 1 || filter.size() > 100)
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        if (filter.keyword() != null && filter.keyword().length() > 200)
            throw new IllegalArgumentException("Keyword must be at most 200 characters");
        if (filter.action() != null && (filter.action().isBlank() || filter.action().length() > 80))
            throw new IllegalArgumentException("Action filter is invalid");
        if (filter.targetType() != null && (filter.targetType().isBlank() || filter.targetType().length() > 40))
            throw new IllegalArgumentException("Target type filter is invalid");
        if (filter.targetId() != null && (filter.targetId().isBlank() || filter.targetId().length() > 200))
            throw new IllegalArgumentException("Target ID filter is invalid");
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to()))
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        if (filter.sortBy() == null || !SORT_FIELDS.contains(filter.sortBy()))
            throw new IllegalArgumentException("Unsupported audit log sort field");
        if (filter.sortDirection() == null || !(filter.sortDirection().equalsIgnoreCase("ASC")
                || filter.sortDirection().equalsIgnoreCase("DESC")))
            throw new IllegalArgumentException("Sort direction must be ASC or DESC");
        return repository.find(filter);
    }
}
