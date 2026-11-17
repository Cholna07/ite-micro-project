package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.dto.result.LoginAttemptUpdate;
import kh.edu.istad.platform.iam.persistence.repository.UserJpaRepository;
import kh.edu.istad.platform.iam.persistence.mapper.UserPersistenceMapper;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AtomicLoginLockoutPersistenceTest {
    private JdbcTemplate jdbc;
    private UserRepositoryAdapter repository;
    private TransactionTemplate transactions;

    @BeforeEach
    void setUp() {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:lockout" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        jdbc = new JdbcTemplate(source);
        transactions = new TransactionTemplate(new DataSourceTransactionManager(source));
        jdbc.execute("CREATE TABLE users (uuid UUID PRIMARY KEY, failed_login_attempts INTEGER NOT NULL, account_non_locked BOOLEAN NOT NULL, enabled BOOLEAN NOT NULL, is_deleted BOOLEAN NOT NULL, last_login_at TIMESTAMP WITH TIME ZONE, updated_at TIMESTAMP WITH TIME ZONE)");
        repository = new UserRepositoryAdapter(mock(UserJpaRepository.class), mock(UserPersistenceMapper.class), jdbc);
    }

    @Test
    void simultaneousFailuresCannotIncrementPastThresholdOrUnlockOnConcurrentSuccess() throws Exception {
        UUID user = UUID.randomUUID();
        jdbc.update("INSERT INTO users (uuid, failed_login_attempts, account_non_locked, enabled, is_deleted) VALUES (?, 0, TRUE, TRUE, FALSE)", user);
        var executor = Executors.newFixedThreadPool(8);
        try {
            List<java.util.concurrent.Future<LoginAttemptUpdate>> attempts = new ArrayList<>();
            for (int i = 0; i < 16; i++) attempts.add(executor.submit(() ->
                    transactions.execute(status -> repository.recordFailedLoginAttempt(user, 5))));
            int newlyLocked = 0;
            for (var attempt : attempts) {
                LoginAttemptUpdate result = attempt.get(10, TimeUnit.SECONDS);
                if (result.changed() && result.locked()) newlyLocked++;
            }
            assertEquals(1, newlyLocked);
            assertEquals(5, jdbc.queryForObject("SELECT failed_login_attempts FROM users WHERE uuid = ?", Integer.class, user));
            assertEquals(false, jdbc.queryForObject("SELECT account_non_locked FROM users WHERE uuid = ?", Boolean.class, user));
            assertFalse(repository.recordSuccessfulLogin(user, java.time.Instant.now()));
            assertEquals(5, jdbc.queryForObject("SELECT failed_login_attempts FROM users WHERE uuid = ?", Integer.class, user));
        } finally {
            executor.shutdownNow();
        }
    }
}
