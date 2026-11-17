package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.UserSession;
import kh.edu.istad.platform.iam.domain.usecase.user.GetCurrentUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.HttpSessionInventory;
import kh.edu.istad.platform.iam.domain.usecase.user.ListUserSessionsUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RevokeUserSessionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.restapi.dto.response.PageMetadata;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import kh.edu.istad.platform.restapi.dto.response.UserSessionResponse;
import kh.edu.istad.platform.restapi.exception.RestApiException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SessionController {
    private final GetCurrentUserUseCase currentUser;
    private final ListUserSessionsUseCase listSessions;
    private final RevokeUserSessionUseCase revokeSession;
    private final RecordAccountActivityEventUseCase recordActivity;

    @GetMapping("/api/me/sessions")
    public ResponseEntity<PageResponse<UserSessionResponse>> listOwn(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            HttpServletRequest request) {
        UUID userUuid = currentUser.execute().uuid();
        UUID currentId = currentSessionId(request);
        return ResponseEntity.ok(toPage(listSessions.execute(userUuid, page, size), currentId));
    }

    @DeleteMapping("/api/me/sessions/{sessionId}")
    @Transactional
    public ResponseEntity<Void> revokeOwn(@PathVariable("sessionId") UUID sessionId, HttpServletRequest request) {
        UUID actor = currentUser.execute().uuid();
        if (!revokeSession.execute(actor, sessionId, actor, "REVOKED_BY_USER")) {
            throw new RestApiException(HttpStatus.NOT_FOUND, "Active session was not found");
        }
        boolean current = sessionId.equals(currentSessionId(request));
        recordActivity.execute(actor, current ? AccountActivityEventType.CURRENT_SESSION_REVOKED
                        : AccountActivityEventType.SESSION_REVOKED,
                current ? "Current session revoked" : "An account session was revoked");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/me/sessions/current")
    @Transactional
    public ResponseEntity<Void> revokeCurrent(HttpServletRequest request) {
        UUID actor = currentUser.execute().uuid();
        UUID currentId = currentSessionId(request);
        if (currentId == null) throw new RestApiException(HttpStatus.CONFLICT, "Current session could not be identified");
        if (!revokeSession.execute(actor, currentId, actor, "LOGOUT"))
            throw new RestApiException(HttpStatus.NOT_FOUND, "Active session was not found");
        recordActivity.execute(actor, AccountActivityEventType.CURRENT_SESSION_REVOKED,
                "Current session revoked");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/me/sessions/others")
    @Transactional
    public ResponseEntity<Void> revokeOthers(HttpServletRequest request) {
        UUID actor = currentUser.execute().uuid();
        try {
            int revoked = revokeSession.revokeOthers(actor, currentSessionId(request), actor);
            if (revoked > 0) recordActivity.execute(actor, AccountActivityEventType.OTHER_SESSIONS_REVOKED,
                    "Other active sessions revoked");
        }
        catch (IllegalStateException exception) { throw new RestApiException(HttpStatus.CONFLICT, exception.getMessage()); }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/me/sessions/all")
    @Transactional
    public ResponseEntity<Void> revokeAll() {
        UUID actor = currentUser.execute().uuid();
        int revoked = revokeSession.revokeAll(actor, actor);
        if (revoked > 0) recordActivity.execute(actor, AccountActivityEventType.OTHER_SESSIONS_REVOKED,
                "All active sessions revoked");
        return ResponseEntity.noContent().build();
    }

    private UUID currentSessionId(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            Object value = session.getAttribute(HttpSessionInventory.SESSION_RECORD_ID);
            if (value instanceof String text) try { return UUID.fromString(text); } catch (IllegalArgumentException ignored) { }
        }
        if (org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                instanceof JwtAuthenticationToken jwt) {
            String value = jwt.getToken().getClaimAsString("iam_session_id");
            if (value != null) try { return UUID.fromString(value); } catch (IllegalArgumentException ignored) { }
        }
        return null;
    }

    private PageResponse<UserSessionResponse> toPage(PageResult<UserSession> page, UUID currentId) {
        List<UserSessionResponse> content = page.content().stream().map(session -> toResponse(session, currentId)).toList();
        return new PageResponse<>(content, new PageMetadata(page.page(), page.size(), page.totalElements(),
                page.totalPages(), page.first(), page.last(), content.isEmpty()));
    }

    private UserSessionResponse toResponse(UserSession session, UUID currentId) {
        return new UserSessionResponse(session.id(), session.credentialType(), session.authenticationType(),
                session.provider(), session.createdAt(), session.lastActivityAt(), session.expiresAt(),
                session.ipAddress(), session.userAgent(), session.id().equals(currentId));
    }
}
