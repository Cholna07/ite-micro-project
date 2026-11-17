package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;
import kh.edu.istad.platform.iam.domain.port.out.SystemConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SystemConfigurationRepositoryAdapter implements SystemConfigurationRepository {
    private static final String REGISTRATION_ENABLED = "registration.enabled";
    private static final String PASSWORD_MINIMUM_LENGTH = "password.minimum-length";
    private static final String MAX_FAILED_ATTEMPTS = "authentication.max-failed-attempts";
    private static final String BRAND_LOGO_URL = "branding.logo-url";
    private final JdbcTemplate jdbc;

    @Override
    @Transactional(readOnly = true)
    public IamSystemConfiguration findCurrent() {
        Map<String, String> values = jdbc.query(
                "SELECT config_key, config_value FROM iam_system_configuration",
                (rs, row) -> Map.entry(rs.getString("config_key"), rs.getString("config_value")))
                .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, duplicate) -> first));
        IamSystemConfiguration defaults = IamSystemConfiguration.DEFAULTS;
        return new IamSystemConfiguration(
                parseBoolean(values.get(REGISTRATION_ENABLED), defaults.registrationEnabled()),
                parseBoundedInteger(values.get(PASSWORD_MINIMUM_LENGTH), 8, 32, defaults.minimumPasswordLength()),
                parseBoundedInteger(values.get(MAX_FAILED_ATTEMPTS), 3, 10, defaults.maxFailedLoginAttempts()),
                values.getOrDefault(BRAND_LOGO_URL, defaults.logoUrl()));
    }

    @Override
    @Transactional
    public void update(IamSystemConfiguration configuration, String updatedBy) {
        upsert(REGISTRATION_ENABLED, Boolean.toString(configuration.registrationEnabled()), updatedBy);
        upsert(PASSWORD_MINIMUM_LENGTH, Integer.toString(configuration.minimumPasswordLength()), updatedBy);
        upsert(MAX_FAILED_ATTEMPTS, Integer.toString(configuration.maxFailedLoginAttempts()), updatedBy);
        upsert(BRAND_LOGO_URL, configuration.logoUrl() == null ? "" : configuration.logoUrl().trim(), updatedBy);
    }

    private void upsert(String key, String value, String updatedBy) {
        int updated = jdbc.update("UPDATE iam_system_configuration SET config_value = ?, "
                        + "updated_at = CURRENT_TIMESTAMP, updated_by = ? WHERE config_key = ?",
                value, updatedBy, key);
        if (updated == 0) {
            jdbc.update("INSERT INTO iam_system_configuration (config_key, config_value, updated_at, updated_by) "
                            + "VALUES (?, ?, CURRENT_TIMESTAMP, ?)", key, value, updatedBy);
        }
    }

    private boolean parseBoolean(String value, boolean fallback) {
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        return fallback;
    }

    private int parseBoundedInteger(String value, int min, int max, int fallback) {
        if (value == null) return fallback;
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed >= min && parsed <= max ? parsed : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
