package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.dto.query.AuditLogFilter;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.AuditLogEvent;
import kh.edu.istad.platform.iam.domain.entity.AuditLogOutcome;
import kh.edu.istad.platform.iam.domain.port.out.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepository {
    private static final java.util.Map<String, String> SORT_COLUMNS = java.util.Map.of(
            "occurredAt", "occurred_at", "action", "action_type", "targetType", "target_type",
            "targetId", "target_id", "outcome", "outcome", "actorUuid", "actor_uuid");
    private final JdbcTemplate jdbc;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void append(AuditLogEvent event) {
        jdbc.update("INSERT INTO admin_audit_events (event_uuid, actor_uuid, action_type, target_type, target_id, "
                        + "outcome, occurred_at, ip_address, user_agent, details) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                event.id(), event.actorUuid(), event.action(), event.targetType(), event.targetId(),
                event.outcome().name(), Timestamp.from(event.occurredAt()), event.ipAddress(), event.userAgent(), event.details());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<AuditLogEvent> find(AuditLogFilter filter) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (filter.keyword() != null && !filter.keyword().isBlank()) {
            where.append(" AND POSITION(LOWER(?) IN LOWER(COALESCE(CAST(actor_uuid AS VARCHAR), '') || ' ' || action_type || ' ' || target_type || ' ' || target_id || ' ' || COALESCE(details, ''))) > 0");
            params.add(filter.keyword().trim());
        }
        add(where, params, "actor_uuid = ?", filter.actorUuid());
        add(where, params, "action_type = ?", filter.action());
        add(where, params, "target_type = ?", filter.targetType());
        add(where, params, "target_id = ?", filter.targetId());
        add(where, params, "outcome = ?", filter.outcome() == null ? null : filter.outcome().name());
        add(where, params, "occurred_at >= ?", filter.from() == null ? null : Timestamp.from(filter.from()));
        add(where, params, "occurred_at <= ?", filter.to() == null ? null : Timestamp.from(filter.to()));

        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM admin_audit_events" + where, Long.class, params.toArray());
        String direction = filter.sortDirection().toUpperCase(java.util.Locale.ROOT);
        String sortColumn = SORT_COLUMNS.get(filter.sortBy());
        String order = filter.sortBy().equals("occurredAt")
                ? "occurred_at " + direction + ", event_uuid " + direction
                : sortColumn + " " + direction + ", occurred_at DESC, event_uuid DESC";
        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(filter.size());
        pageParams.add((long) filter.page() * filter.size());
        List<AuditLogEvent> content = jdbc.query("SELECT event_uuid, actor_uuid, action_type, target_type, target_id, outcome, "
                        + "occurred_at, ip_address, user_agent, details FROM admin_audit_events" + where
                        + " ORDER BY " + order + " LIMIT ? OFFSET ?", this::map, pageParams.toArray());
        long totalElements = total == null ? 0 : total;
        int totalPages = (int) Math.ceil((double) totalElements / filter.size());
        return new PageResult<>(content, filter.page(), filter.size(), totalElements, totalPages,
                filter.page() == 0, filter.page() >= totalPages - 1);
    }

    private void add(StringBuilder where, List<Object> params, String clause, Object value) {
        if (value == null) return;
        where.append(" AND ").append(clause);
        params.add(value);
    }

    private AuditLogEvent map(ResultSet rs, int row) throws SQLException {
        UUID actor = (UUID) rs.getObject("actor_uuid");
        return new AuditLogEvent(rs.getObject("event_uuid", UUID.class), actor, rs.getString("action_type"),
                rs.getString("target_type"), rs.getString("target_id"), AuditLogOutcome.valueOf(rs.getString("outcome")),
                rs.getTimestamp("occurred_at").toInstant(), rs.getString("ip_address"), rs.getString("user_agent"),
                rs.getString("details"));
    }
}
