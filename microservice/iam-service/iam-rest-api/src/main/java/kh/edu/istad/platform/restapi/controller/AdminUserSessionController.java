package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.UserSession;
import kh.edu.istad.platform.iam.domain.usecase.user.ListUserSessionsUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RevokeUserSessionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.GetCurrentUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.restapi.dto.response.PageMetadata;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import kh.edu.istad.platform.restapi.dto.response.UserSessionResponse;
import kh.edu.istad.platform.restapi.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/admin/users/{userUuid}/sessions")
@RequiredArgsConstructor
public class AdminUserSessionController {
    private final ListUserSessionsUseCase listSessions;
    private final RevokeUserSessionUseCase revokeSession;
    private final GetCurrentUserUseCase currentUser;
    private final RecordAccountActivityEventUseCase recordActivity;

    @GetMapping
    public ResponseEntity<PageResponse<UserSessionResponse>> list(@PathVariable("userUuid") UUID userUuid,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {
        try {
            PageResult<UserSession> result = listSessions.execute(userUuid, page, size);
            List<UserSessionResponse> content = result.content().stream().map(this::toResponse).toList();
            return ResponseEntity.ok(new PageResponse<>(content, new PageMetadata(result.page(), result.size(),
                    result.totalElements(), result.totalPages(), result.first(), result.last(), content.isEmpty())));
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @DeleteMapping("/{sessionId}")
    @Transactional
    public ResponseEntity<Void> revokeOne(@PathVariable("userUuid") UUID userUuid, @PathVariable("sessionId") UUID sessionId) {
        UUID actor = currentUser.execute().uuid();
        if (!revokeSession.execute(userUuid, sessionId, actor, "REVOKED_BY_ADMIN"))
            throw new RestApiException(HttpStatus.NOT_FOUND, "Active session was not found for this user");
        recordActivity.execute(userUuid, AccountActivityEventType.SESSION_REVOKED,
                "An account session was revoked by an administrator");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<Void> revokeAll(@PathVariable("userUuid") UUID userUuid) {
        int revoked = revokeSession.revokeAll(userUuid, currentUser.execute().uuid());
        if (revoked > 0) recordActivity.execute(userUuid, AccountActivityEventType.OTHER_SESSIONS_REVOKED,
                "Account sessions were revoked by an administrator");
        return ResponseEntity.noContent().build();
    }

    private UserSessionResponse toResponse(UserSession session) {
        return new UserSessionResponse(session.id(), session.credentialType(), session.authenticationType(),
                session.provider(), session.createdAt(), session.lastActivityAt(), session.expiresAt(),
                session.ipAddress(), session.userAgent(), false);
    }
}
