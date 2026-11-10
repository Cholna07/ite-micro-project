package kh.edu.istad.platform.iam.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class InfrastructureConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ApplicationRunner seedRegistrationClient(
            RegisteredClientRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${iam.oauth2.registration.client-id}") String clientId,
            @Value("${iam.oauth2.registration.client-secret}") String clientSecret
    ) {
        return args -> {

            if (repository.findByClientId(clientId) != null) {
                return;
            }

            RegisteredClient registrationClient =
                    RegisteredClient.withId(UUID.randomUUID().toString())
                            .clientId(clientId)
                            .clientSecret(passwordEncoder.encode(clientSecret))
                            .clientName("IAM Dynamic Client Registration")
                            .clientAuthenticationMethod(
                                    ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                            )
                            .authorizationGrantType(
                                    AuthorizationGrantType.CLIENT_CREDENTIALS
                            )
                            .scope("client.create")
                            .clientSettings(
                                    ClientSettings.builder()
                                            .requireAuthorizationConsent(false)
                                            .build()
                            )
                            .tokenSettings(
                                    TokenSettings.builder()
                                            .accessTokenTimeToLive(
                                                    Duration.ofMinutes(10)
                                            )
                                            .build()
                            )
                            .build();

            repository.save(registrationClient);
        };
    }
}
