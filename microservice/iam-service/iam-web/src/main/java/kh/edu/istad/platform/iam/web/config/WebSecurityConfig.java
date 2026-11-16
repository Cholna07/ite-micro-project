package kh.edu.istad.platform.iam.web.config;

import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordLoginEventUseCase;
import kh.edu.istad.platform.iam.domain.dto.command.BrowserSessionContext;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.RegisterBrowserSessionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.HttpSessionInventory;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import java.util.UUID;

@Configuration
public class WebSecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Bean
    @Order(4)
    SecurityFilterChain browserSecurityFilterChain(
            HttpSecurity http,
            CookieCsrfTokenRepository csrfTokenRepository,
            @Qualifier("spaCsrfCookieFilter") Filter spaCsrfCookieFilter,
            RecordLoginEventUseCase recordLoginEventUseCase,
            RegisterBrowserSessionUseCase registerBrowserSession,
            UserSessionRepository userSessionRepository,
            RecordAccountActivityEventUseCase recordAccountActivityEventUseCase,
            @Qualifier("sessionRevocationFilter") Filter sessionRevocationFilter
    ) throws Exception {
        http.securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/frontend/**", "/assets/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/profile/**", "/account", "/account/**", "/users", "/users/**", "/roles", "/roles/**", "/permissions", "/permissions/**", "/clients", "/clients/**", "/admin", "/admin/**", "/audit-logs", "/system-config").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            recordWebLoginSafely(recordLoginEventUseCase, authentication.getName(),
                                    request, LoginEventOutcome.SUCCESS, null);
                            try {
                                var session = request.getSession(true);
                                UUID inventoryId = registerBrowserSession.execute(authentication.getName(),
                                        new BrowserSessionContext(request.getRemoteAddr(), request.getHeader("User-Agent"),
                                                "PASSWORD", null, session.getMaxInactiveInterval()));
                                session.setAttribute(HttpSessionInventory.SESSION_RECORD_ID, inventoryId.toString());
                                new SavedRequestAwareAuthenticationSuccessHandler()
                                        .onAuthenticationSuccess(request, response, authentication);
                            } catch (RuntimeException exception) {
                                org.springframework.security.core.context.SecurityContextHolder.clearContext();
                                var session = request.getSession(false);
                                if (session != null) try { session.invalidate(); } catch (IllegalStateException ignored) { }
                                response.sendError(503, "Unable to establish an authenticated session");
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            recordWebLoginSafely(recordLoginEventUseCase, request.getParameter("username"),
                                    request, LoginEventOutcome.FAILURE, "INVALID_CREDENTIALS");
                            new SimpleUrlAuthenticationFailureHandler("/login?flow=oauth&error")
                                    .onAuthenticationFailure(request, response, exception);
                        }).permitAll())
                .logout(logout -> logout
                        .addLogoutHandler(new InventoryLogoutHandler(userSessionRepository, recordAccountActivityEventUseCase))
                        .logoutSuccessUrl("/login?logout").permitAll())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .addFilterAfter(spaCsrfCookieFilter, CsrfFilter.class);
        http.addFilterAfter(sessionRevocationFilter, SecurityContextHolderFilter.class);
        return http.build();
    }

    static void recordWebLoginSafely(RecordLoginEventUseCase recorder, String username,
                                     jakarta.servlet.http.HttpServletRequest request,
                                     LoginEventOutcome outcome, String failureCategory) {
        try {
            recorder.execute(username, outcome, request.getRemoteAddr(), request.getHeader("User-Agent"), failureCategory);
        } catch (RuntimeException exception) {
            log.error("Could not persist browser login event with outcome {}", outcome, exception);
        }
    }
}
