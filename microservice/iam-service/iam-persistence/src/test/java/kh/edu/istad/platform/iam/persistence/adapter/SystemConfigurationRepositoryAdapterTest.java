package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemConfigurationRepositoryAdapterTest {
    private JdbcTemplate jdbc;
    private SystemConfigurationRepositoryAdapter repository;

    @BeforeEach
    void setUp() {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:system-config" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE iam_system_configuration (config_key VARCHAR(100) PRIMARY KEY, config_value TEXT NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_by VARCHAR(100))");
        repository = new SystemConfigurationRepositoryAdapter(jdbc);
    }

    @Test
    void missingOrMalformedValuesUseSafeDefaults() {
        assertEquals(IamSystemConfiguration.DEFAULTS, repository.findCurrent());
        jdbc.update("INSERT INTO iam_system_configuration (config_key, config_value) VALUES (?, ?)", "registration.enabled", "sometimes");
        jdbc.update("INSERT INTO iam_system_configuration (config_key, config_value) VALUES (?, ?)", "password.minimum-length", "999");
        jdbc.update("INSERT INTO iam_system_configuration (config_key, config_value) VALUES (?, ?)", "authentication.max-failed-attempts", "bad");
        assertEquals(IamSystemConfiguration.DEFAULTS, repository.findCurrent());
    }

    @Test
    void updatesPersistAcrossRepositoryReconstructionAndDoNotCreateDuplicates() {
        IamSystemConfiguration configured = new IamSystemConfiguration(false, 12, 7);
        repository.update(configured, "administrator");
        repository = new SystemConfigurationRepositoryAdapter(jdbc);
        assertEquals(configured, repository.findCurrent());
        repository.update(new IamSystemConfiguration(true, 10, 6), "administrator-2");
        assertEquals(4, jdbc.queryForObject("SELECT COUNT(*) FROM iam_system_configuration", Integer.class));
        assertEquals(new IamSystemConfiguration(true, 10, 6), repository.findCurrent());
    }

    @Test
    void persistsLogoUrlWithoutChangingSecuritySettings() {
        IamSystemConfiguration configured = new IamSystemConfiguration(true, 12, 6,
                "https://assets.example.edu/istad-logo.png");

        repository.update(configured, "administrator");

        assertEquals(configured, repository.findCurrent());
    }
}
