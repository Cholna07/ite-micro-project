package kh.edu.istad.platform.iam.infrastructure.oauth2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration
public class RegisteredClientConfig {

    @Bean("oauth2RegisteredClientStorage")
    public RegisteredClientRepository oauth2RegisteredClientStorage(
            JdbcTemplate jdbcTemplate
    ) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    /**
     * The authorization server resolves a client by client_id for new
     * authentication and authorization requests. Hiding disabled clients at
     * this lookup enforces the status stored in the existing client_settings
     * column without deleting client or consent history.
     */
    @Bean
    @Primary
    public RegisteredClientRepository registeredClientRepository(
            @org.springframework.beans.factory.annotation.Qualifier("oauth2RegisteredClientStorage")
            RegisteredClientRepository storage
    ) {
        return new ActiveRegisteredClientRepository(storage);
    }
}
