
package kh.edu.istad.platform.restapi.config;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    @Order(3)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CookieCsrfTokenRepository csrfTokenRepository,
            @Qualifier("spaCsrfCookieFilter") Filter spaCsrfCookieFilter,
            @Qualifier("sessionRevocationFilter") Filter sessionRevocationFilter
    )
            throws Exception {

        http
                .securityMatcher("/api/**", "/connect/**")
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/auth/login", "/api/auth/register"))

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .authorizeHttpRequests(auth -> auth

                        // Public registration and login API
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/providers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/branding").permitAll()

                        // DCR GET: controller validates registration access token
                        .requestMatchers(
                                HttpMethod.GET,
                                "/connect/register"
                        ).permitAll()

                        // DCR POST: requires authentication
                        // Controller additionally checks the client.create scope
                        .requestMatchers(
                                HttpMethod.POST,
                                "/connect/register"
                        ).authenticated()

                        // Administrative OAuth client inventory and configuration.
                        .requestMatchers(HttpMethod.GET, "/api/admin/clients", "/api/admin/clients/**")
                        .hasAuthority("client.read")
                        .requestMatchers(HttpMethod.POST, "/api/admin/clients")
                        .hasAuthority("client.manage")
                        .requestMatchers(HttpMethod.POST, "/api/admin/clients/*/rotate-secret")
                        .hasAuthority("client.manage")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/clients/**")
                        .hasAuthority("client.manage")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/clients/**")
                        .hasAuthority("client.manage")

                        // User access assignment mutations require explicit IAM permissions.
                        .requestMatchers(HttpMethod.POST, "/api/users/*/roles")
                        .hasAuthority("user.role.assign")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*/roles/*")
                        .hasAuthority("user.role.assign")
                        .requestMatchers(HttpMethod.POST, "/api/users/*/permissions")
                        .hasAuthority("user.permission.assign")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*/permissions/*")
                        .hasAuthority("user.permission.assign")

                        .requestMatchers(HttpMethod.POST, "/api/roles/*/permissions")
                        .hasAuthority("role.permission.assign")
                        .requestMatchers(HttpMethod.DELETE, "/api/roles/*/permissions/*")
                        .hasAuthority("role.permission.assign")

                        // Administrative login history is limited to users authorized to read users.
                        .requestMatchers(HttpMethod.GET, "/api/admin/login-history")
                        .hasAuthority("user.read")
                        .requestMatchers(HttpMethod.GET, "/api/admin/account-activities")
                        .hasAuthority("activity.read")
                        .requestMatchers(HttpMethod.GET, "/api/admin/audit-logs")
                        .hasAuthority("audit.read")
                        .requestMatchers(HttpMethod.GET, "/api/admin/system-config")
                        .hasAuthority("system.config.read")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/system-config")
                        .hasAuthority("system.config.manage")

                        .requestMatchers(HttpMethod.GET, "/api/admin/users/*/sessions")
                        .hasAuthority("session.read")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/users/*/sessions", "/api/admin/users/*/sessions/**")
                        .hasAuthority("session.revoke")

                        .requestMatchers(HttpMethod.GET, "/api/users/**")
                        .hasAuthority("user.read")
                        .requestMatchers(HttpMethod.POST, "/api/users/**")
                        .hasAuthority("user.manage")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**")
                        .hasAuthority("user.manage")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**")
                        .hasAuthority("user.manage")

                        .requestMatchers(HttpMethod.GET, "/api/roles/**")
                        .hasAuthority("role.read")
                        .requestMatchers(HttpMethod.POST, "/api/roles/**")
                        .hasAuthority("role.manage")
                        .requestMatchers(HttpMethod.PUT, "/api/roles/**")
                        .hasAuthority("role.manage")
                        .requestMatchers(HttpMethod.DELETE, "/api/roles/**")
                        .hasAuthority("role.manage")

                        .requestMatchers(HttpMethod.GET, "/api/permissions/**")
                        .hasAuthority("permission.read")
                        .requestMatchers(HttpMethod.POST, "/api/permissions/**")
                        .hasAuthority("permission.manage")
                        .requestMatchers(HttpMethod.PUT, "/api/permissions/**")
                        .hasAuthority("permission.manage")
                        .requestMatchers(HttpMethod.DELETE, "/api/permissions/**")
                        .hasAuthority("permission.manage")

                        // Self-service account routes use the authenticated principal.
                        .requestMatchers("/api/me/**").authenticated()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(request -> {

                            // Registration GET uses an opaque registration
                            // access token, not a JWT.
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

        http.addFilterAfter(spaCsrfCookieFilter, CsrfFilter.class);
        http.addFilterAfter(sessionRevocationFilter, SecurityContextHolderFilter.class);

        return http.build();
    }
}
