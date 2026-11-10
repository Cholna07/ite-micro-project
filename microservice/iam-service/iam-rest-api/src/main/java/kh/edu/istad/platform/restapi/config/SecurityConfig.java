package kh.edu.istad.platform.restapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Public registration and login
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // DCR GET: controller validates the registration access token
                        .requestMatchers(
                                HttpMethod.GET,
                                "/connect/register"
                        ).permitAll()

                        // DCR POST: requires authentication.
                        // Controller additionally checks the client.create scope.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/connect/register"
                        ).authenticated()

                        // User profile
                        .requestMatchers("/api/me").authenticated()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(request -> {

                            // Registration GET uses an opaque registration access token,
                            // not a JWT.
                            if ("GET".equals(request.getMethod())
                                    && "/connect/register".equals(request.getServletPath())) {
                                return null;
                            }

                            return new DefaultBearerTokenResolver().resolve(request);
                        })
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                );

        return http.build();
    }
}