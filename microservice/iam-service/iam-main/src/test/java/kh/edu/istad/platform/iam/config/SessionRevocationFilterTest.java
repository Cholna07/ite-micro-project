package kh.edu.istad.platform.iam.config;

import kh.edu.istad.platform.iam.domain.entity.SessionCredentialType;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserSession;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.HttpSessionInventory;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.AuthorityUtils;

import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SessionRevocationFilterTest {
    private final UserSessionRepository sessions = mock(UserSessionRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final SessionRevocationConfiguration.SessionRevocationFilter filter =
            new SessionRevocationConfiguration.SessionRevocationFilter(sessions, users, Duration.ofHours(8));

    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void allowsAnActiveOwnedSessionAndRefreshesActivity() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        authenticate("alice");
        MockHttpServletRequest request = request("/api/me/sessions");
        MockHttpSession session = new MockHttpSession();
        session.setMaxInactiveInterval(600);
        session.setAttribute(HttpSessionInventory.SESSION_RECORD_ID, id.toString());
        request.setSession(session);
        User alice = user(userId);
        when(users.findByUsernameOrEmail("alice")).thenReturn(Optional.of(alice));
        when(users.findByUuid(userId)).thenReturn(Optional.of(alice));
        when(sessions.findById(id)).thenReturn(Optional.of(record(id, userId, null)));
        when(sessions.updateActivity(eq(id), any(), any())).thenReturn(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(sessions).updateActivity(eq(id), any(), any());
        assertEquals(200, response.getStatus());
    }

    @Test
    void rejectsAnActiveSessionPastItsAbsoluteLifetime() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        authenticate("alice");
        MockHttpServletRequest request = request("/api/me");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionInventory.SESSION_RECORD_ID, id.toString());
        request.setSession(session);
        User alice = user(userId);
        when(users.findByUsernameOrEmail("alice")).thenReturn(Optional.of(alice));
        when(users.findByUuid(userId)).thenReturn(Optional.of(alice));
        Instant now = Instant.now();
        when(sessions.findById(id)).thenReturn(Optional.of(new UserSession(id, userId,
                SessionCredentialType.HTTP_SESSION, "PASSWORD", null, now.minus(Duration.ofHours(9)),
                now.minusSeconds(1), now.plusSeconds(500), null, null, null, null, null)));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertEquals(401, response.getStatus());
        assertTrue(session.isInvalid());
        verify(sessions, never()).updateActivity(any(), any(), any());
    }

    @Test
    void preservesTheSessionWhenTheInventoryIsUnavailable() throws Exception {
        UUID id = UUID.randomUUID();
        authenticate("alice");
        MockHttpServletRequest request = request("/api/me");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionInventory.SESSION_RECORD_ID, id.toString());
        request.setSession(session);
        when(sessions.findById(id)).thenThrow(new IllegalStateException("Inventory unavailable"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertEquals(503, response.getStatus());
        assertFalse(session.isInvalid());
    }

    @Test
    void rejectsRevokedSessionAndInvalidatesItsHttpSession() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        authenticate("alice");
        MockHttpServletRequest request = request("/api/me/profile");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionInventory.SESSION_RECORD_ID, id.toString());
        request.setSession(session);
        User alice = user(userId);
        when(users.findByUsernameOrEmail("alice")).thenReturn(Optional.of(alice));
        when(sessions.findById(id)).thenReturn(Optional.of(record(id, userId, Instant.now())));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(session.isInvalid());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void rejectsMissingSessionRecordAndDoesNotTreatBearerTokenAsBrowserSession() throws Exception {
        UUID missingId = UUID.randomUUID();
        authenticate("alice");
        MockHttpServletRequest browserRequest = request("/api/me/profile");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionInventory.SESSION_RECORD_ID, missingId.toString());
        browserRequest.setSession(session);
        User alice = user(UUID.randomUUID());
        when(users.findByUsernameOrEmail("alice")).thenReturn(Optional.of(alice));
        when(sessions.findById(missingId)).thenReturn(Optional.empty());
        MockHttpServletResponse rejected = new MockHttpServletResponse();
        filter.doFilter(browserRequest, rejected, mock(FilterChain.class));
        assertEquals(401, rejected.getStatus());

        MockHttpServletRequest bearerRequest = request("/api/me/profile");
        bearerRequest.addHeader("Authorization", "Bearer opaque-token");
        MockHttpServletResponse accepted = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(bearerRequest, accepted, chain);
        verify(chain).doFilter(bearerRequest, accepted);
    }

    @Test
    void rejectsSessionOwnedByAnotherUserAndExpiredInventoryRecord() throws Exception {
        UUID ownerUuid = UUID.randomUUID();
        UUID otherUserUuid = UUID.randomUUID();
        UUID mismatchedId = UUID.randomUUID();
        authenticate("alice");
        User alice = user(ownerUuid);
        when(users.findByUsernameOrEmail("alice")).thenReturn(Optional.of(alice));
        when(sessions.findById(mismatchedId)).thenReturn(Optional.of(record(mismatchedId, otherUserUuid, null)));
        MockHttpServletRequest mismatchedRequest = request("/api/me/profile");
        MockHttpSession mismatchedSession = new MockHttpSession();
        mismatchedSession.setAttribute(HttpSessionInventory.SESSION_RECORD_ID, mismatchedId.toString());
        mismatchedRequest.setSession(mismatchedSession);
        MockHttpServletResponse mismatchedResponse = new MockHttpServletResponse();
        filter.doFilter(mismatchedRequest, mismatchedResponse, mock(FilterChain.class));
        assertEquals(401, mismatchedResponse.getStatus());

        UUID expiredId = UUID.randomUUID();
        Instant now = Instant.now();
        UserSession expired = new UserSession(expiredId, ownerUuid, SessionCredentialType.HTTP_SESSION,
                "PASSWORD", null, now.minusSeconds(100), now.minusSeconds(90), now.minusSeconds(1),
                null, null, null, null, null);
        when(sessions.findById(expiredId)).thenReturn(Optional.of(expired));
        authenticate("alice");
        MockHttpServletRequest expiredRequest = request("/api/me/profile");
        MockHttpSession expiredSession = new MockHttpSession();
        expiredSession.setAttribute(HttpSessionInventory.SESSION_RECORD_ID, expiredId.toString());
        expiredRequest.setSession(expiredSession);
        MockHttpServletResponse expiredResponse = new MockHttpServletResponse();
        filter.doFilter(expiredRequest, expiredResponse, mock(FilterChain.class));
        assertEquals(401, expiredResponse.getStatus());
    }

    @Test
    void rejectsBrowserSessionWhenAccountHasBeenDisabled() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        authenticate("alice");
        MockHttpServletRequest request = request("/api/me/profile");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionInventory.SESSION_RECORD_ID, id.toString());
        request.setSession(session);
        User alice = user(userId);
        when(users.findByUsernameOrEmail("alice")).thenReturn(Optional.of(alice));
        when(users.findByUuid(userId)).thenReturn(Optional.empty());
        when(sessions.findById(id)).thenReturn(Optional.of(record(id, userId, null)));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertEquals(401, response.getStatus());
        assertTrue(session.isInvalid());
    }

    @Test
    void rejectsBrowserSessionWhenAccountHasBeenLocked() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        authenticate("alice");
        MockHttpServletRequest request = request("/api/me/profile");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionInventory.SESSION_RECORD_ID, id.toString());
        request.setSession(session);
        User alice = user(userId);
        when(alice.isAccountNonLocked()).thenReturn(false);
        when(users.findByUsernameOrEmail("alice")).thenReturn(Optional.of(alice));
        when(users.findByUuid(userId)).thenReturn(Optional.of(alice));
        when(sessions.findById(id)).thenReturn(Optional.of(record(id, userId, null)));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertEquals(401, response.getStatus());
        assertTrue(session.isInvalid());
    }

    private void authenticate(String name) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(name, "", AuthorityUtils.NO_AUTHORITIES));
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        request.setServletPath(path);
        return request;
    }

    private User user(UUID id) {
        User user = mock(User.class);
        when(user.getUuid()).thenReturn(id);
        when(user.isEnabled()).thenReturn(true);
        when(user.isDeleted()).thenReturn(false);
        when(user.isAccountNonLocked()).thenReturn(true);
        return user;
    }

    private UserSession record(UUID id, UUID userId, Instant revokedAt) {
        Instant now = Instant.now();
        return new UserSession(id, userId, SessionCredentialType.HTTP_SESSION, "PASSWORD", null,
                now.minusSeconds(30), now.minusSeconds(1), now.plusSeconds(500), "192.0.2.1", "Agent",
                revokedAt, null, revokedAt == null ? null : "REVOKED_BY_ADMIN");
    }
}
