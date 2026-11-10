
package kh.edu.istad.platform.iam.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Configuration
public class InfrastructureConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

        DelegatingPasswordEncoder delegating =
                new DelegatingPasswordEncoder(
                        "bcrypt",
                        Map.of("bcrypt", bcrypt)
                );

        // Support existing BCrypt hashes without the {bcrypt} prefix.
        delegating.setDefaultPasswordEncoderForMatches(bcrypt);

        return delegating;
    }

    @Bean
    public ApplicationRunner seedRegistrationClient(
            RegisteredClientRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${iam.oauth2.registration.client-id}") String clientId,
            @Value("${iam.oauth2.registration.client-secret}") String clientSecret
    ) {
        return args -> {

            // Avoid creating the same registration client twice.
            RegisteredClient existingClient =
                    repository.findByClientId(clientId);

            if (existingClient != null) {
                boolean secretMatches = passwordEncoder.matches(
                        clientSecret,
                        existingClient.getClientSecret()
                );

                System.out.println(
                        "Registration client exists: " + clientId
                );

                System.out.println(
                        "Registration client secret matches: " + secretMatches
                );

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
                                            .accessTokenTimeToLive(Duration.ofMinutes(10))
                                            .build()
                            )
                            .build();

            repository.save(registrationClient);
        };
    }
}
