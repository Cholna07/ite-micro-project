package kh.edu.istad.platform.iam.domain.port.out;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.dto.query.AuditLogFilter;
import kh.edu.istad.platform.iam.domain.entity.AuditLogEvent;

public interface AuditLogRepository {
    void append(AuditLogEvent event);
    PageResult<AuditLogEvent> find(AuditLogFilter filter);
}
