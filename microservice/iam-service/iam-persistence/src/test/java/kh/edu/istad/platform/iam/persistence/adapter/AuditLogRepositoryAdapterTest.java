package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.dto.query.AuditLogFilter;
import kh.edu.istad.platform.iam.domain.entity.AuditLogEvent;
import kh.edu.istad.platform.iam.domain.entity.AuditLogOutcome;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogRepositoryAdapterTest {
    private JdbcTemplate jdbc;
    private AuditLogRepositoryAdapter repository;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:audit" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE admin_audit_events (event_uuid UUID PRIMARY KEY, actor_uuid UUID, action_type VARCHAR(80) NOT NULL, target_type VARCHAR(40) NOT NULL, target_id VARCHAR(200) NOT NULL, outcome VARCHAR(10) NOT NULL, occurred_at TIMESTAMP WITH TIME ZONE NOT NULL, ip_address VARCHAR(45), user_agent VARCHAR(512), details TEXT)");
        repository = new AuditLogRepositoryAdapter(jdbc);
    }

    @Test
    void appendsAndRetrievesEventsWithStableNewestFirstOrderingAndFilters() {
        UUID actor = UUID.randomUUID();
        Instant timestamp = Instant.parse("2026-01-01T12:00:00Z");
        UUID olderId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID newerId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        repository.append(new AuditLogEvent(olderId, actor, "USER_LOCKED", "USER", "u-1",
                AuditLogOutcome.SUCCESS, timestamp, "192.0.2.1", "Browser", null));
        repository.append(new AuditLogEvent(newerId, actor, "USER_UNLOCKED", "USER", "u-1",
                AuditLogOutcome.SUCCESS, timestamp, "192.0.2.1", "Browser", null));

        AuditLogFilter all = new AuditLogFilter(null, null, null, null, null, null,
                null, null, 0, 10, "occurredAt", "DESC");
        var page = repository.find(all);
        assertEquals(2, page.totalElements());
        assertEquals(newerId, page.content().getFirst().id());
        assertEquals(olderId, page.content().get(1).id());

        AuditLogFilter filtered = new AuditLogFilter("unlock", actor, "USER_UNLOCKED", "USER", "u-1",
                AuditLogOutcome.SUCCESS, timestamp, timestamp, 0, 10, "occurredAt", "DESC");
        var matching = repository.find(filtered);
        assertEquals(1, matching.totalElements());
        assertEquals("USER_UNLOCKED", matching.content().getFirst().action());
    }

    @Test
    void historicalActorReferencesRemainUnconstrainedByUserDeletion() {
        UUID actor = UUID.randomUUID();
        repository.append(new AuditLogEvent(UUID.randomUUID(), actor, "ROLE_DELETED", "ROLE", "r-1",
                AuditLogOutcome.SUCCESS, Instant.now(), null, null, null));
        jdbc.execute("CREATE TABLE users (uuid UUID PRIMARY KEY)");
        jdbc.update("INSERT INTO users (uuid) VALUES (?)", actor);
        jdbc.update("DELETE FROM users WHERE uuid = ?", actor);
        var result = repository.find(new AuditLogFilter(null, actor, null, null, null, null,
                null, null, 0, 10, "occurredAt", "DESC"));
        assertEquals(1, result.totalElements());
        assertEquals(actor, result.content().getFirst().actorUuid());
    }
}
