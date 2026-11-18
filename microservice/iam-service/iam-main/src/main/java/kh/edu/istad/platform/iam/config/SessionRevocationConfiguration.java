package kh.edu.istad.platform.iam.config;

import kh.edu.istad.platform.iam.domain.entity.UserSession;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.HttpSessionInventory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Configuration
public class SessionRevocationConfiguration {
    @Bean("sessionRevocationFilter")
    SessionRevocationFilter sessionRevocationFilter(UserSessionRepository sessions, UserRepository users,
            @Value("${iam.browser-session.absolute-timeout:8h}") Duration absoluteTimeout) {
        return new SessionRevocationFilter(sessions, users, absoluteTimeout);
    }

    @Bean
    FilterRegistrationBean<SessionRevocationFilter> disableSessionRevocationContainerRegistration(
            SessionRevocationFilter filter) {
        FilterRegistrationBean<SessionRevocationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    static final class SessionRevocationFilter extends OncePerRequestFilter {
        private final UserSessionRepository sessions;
        private final UserRepository users;
        private final Duration absoluteTimeout;

        SessionRevocationFilter(UserSessionRepository sessions, UserRepository users, Duration absoluteTimeout) {
            this.sessions = sessions;
            this.users = users;
            this.absoluteTimeout = absoluteTimeout;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain chain) throws ServletException, IOException {
            if (hasBearerToken(request)) {
                chain.doFilter(request, response);
                return;
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                chain.doFilter(request, response);
                return;
            }

            var httpSession = request.getSession(false);
            Object sessionIdValue = httpSession == null ? null
                    : httpSession.getAttribute(HttpSessionInventory.SESSION_RECORD_ID);
            UserSession stored = null;
            try {
                UUID sessionId = sessionIdValue instanceof String value ? UUID.fromString(value) : null;
                if (sessionId != null) stored = sessions.findById(sessionId).orElse(null);
                UUID authenticatedUser = resolveUserUuid(authentication.getName(), stored);
                boolean accountEnabled = stored != null && users.findByUuid(stored.userUuid())
                        .filter(user -> user.isEnabled() && !user.isDeleted() && user.isAccountNonLocked()).isPresent();
                Instant now = Instant.now();
                if (stored == null || !stored.isActiveAt(now)
                        || !now.isBefore(stored.createdAt().plus(absoluteTimeout))
                        || authenticatedUser == null || !stored.userUuid().equals(authenticatedUser)
                        || !accountEnabled
                        || stored.credentialType() != kh.edu.istad.platform.iam.domain.entity.SessionCredentialType.HTTP_SESSION) {
                    reject(request, response, httpSession);
                    return;
                }
                int interval = httpSession.getMaxInactiveInterval();
                Instant maximumExpiry = stored.createdAt().plus(absoluteTimeout);
                Instant idleExpiry = interval <= 0 ? maximumExpiry : now.plusSeconds(interval);
                Instant expiry = idleExpiry.isBefore(maximumExpiry) ? idleExpiry : maximumExpiry;
                if (!sessions.updateActivity(stored.id(), now, expiry)) {
                    reject(request, response, httpSession);
                    return;
                }
            } catch (RuntimeException exception) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to verify session");
                return;
            }
            chain.doFilter(request, response);
        }

        private UUID resolveUserUuid(String principalName, UserSession storedSession) {
            try {
                return UUID.fromString(principalName);
            } catch (RuntimeException ignored) {
                return users.findByUsernameOrEmail(principalName).map(user -> user.getUuid())
                        .orElse(storedSession == null ? null : storedSession.userUuid());
            }
        }

        private static boolean hasBearerToken(HttpServletRequest request) {
            String header = request.getHeader("Authorization");
            return header != null && header.regionMatches(true, 0, "Bearer ", 0, 7);
        }

        private static void reject(HttpServletRequest request, HttpServletResponse response,
                                   jakarta.servlet.http.HttpSession session) throws IOException {
            SecurityContextHolder.clearContext();
            if (session != null) {
                try { session.invalidate(); } catch (IllegalStateException ignored) { }
            }
            if (request.getServletPath().startsWith("/api/") || request.getServletPath().startsWith("/connect/")
                    || "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session is no longer active");
            } else {
                response.sendRedirect(request.getContextPath() + "/login?session-expired");
            }
        }
    }
}
