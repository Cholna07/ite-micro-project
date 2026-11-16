package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.domain.dto.result.IdentityResult;
import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.dto.command.ChangePasswordCommand;
import kh.edu.istad.platform.iam.domain.dto.command.UpdateUserProfileCommand;
import kh.edu.istad.platform.iam.domain.usecase.identity.ListUserIdentitiesUseCase;
import kh.edu.istad.platform.iam.domain.usecase.identity.IdentityLinkSession;
import kh.edu.istad.platform.iam.domain.usecase.identity.UnlinkIdentityUseCase;
import kh.edu.istad.platform.iam.domain.valueobject.IdentityProvider;
import kh.edu.istad.platform.iam.domain.usecase.user.DisableUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.GetCurrentUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListLoginHistoryUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListAccountActivityUseCase;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEvent;
import kh.edu.istad.platform.iam.domain.entity.LoginEvent;
import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.usecase.user.ChangePasswordUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.UpdateUserProfileUseCase;
import kh.edu.istad.platform.restapi.dto.request.ChangePasswordRequest;
import kh.edu.istad.platform.restapi.dto.request.UpdateUserProfileRequest;
import kh.edu.istad.platform.restapi.exception.RestApiException;
import kh.edu.istad.platform.restapi.dto.response.LinkedIdentityResponse;
import kh.edu.istad.platform.restapi.dto.response.IdentityLinkInitiationResponse;
import kh.edu.istad.platform.restapi.dto.response.UserResponse;
import kh.edu.istad.platform.restapi.dto.response.LoginEventResponse;
import kh.edu.istad.platform.restapi.dto.response.PageMetadata;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import kh.edu.istad.platform.restapi.dto.response.AccountActivityEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final ListUserIdentitiesUseCase listUserIdentitiesUseCase;
    private final UnlinkIdentityUseCase unlinkIdentityUseCase;
    private final DisableUserUseCase disableUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final ListLoginHistoryUseCase listLoginHistoryUseCase;
    private final ListAccountActivityUseCase listAccountActivityUseCase;

    @GetMapping
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {

        UserResult result = getCurrentUserUseCase.execute();
        String permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.contains("."))
                .distinct()
                .collect(java.util.stream.Collectors.joining(","));

        return ResponseEntity.ok()
                .header("X-IAM-Permissions", permissions)
                .body(toResponse(result));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UpdateUserProfileRequest request) {
        UUID userUuid = getCurrentUserUseCase.execute().uuid();
        UserResult result;
        try {
            result = updateUserProfileUseCase.execute(new UpdateUserProfileCommand(
                    userUuid, request.username(), request.email(), request.givenName(), request.familyName(),
                    request.middleName(), request.nickname(), request.gender(), request.birthdate(), request.profileUrl(),
                    request.pictureUrl(), request.websiteUrl(), request.zoneInfo(), request.locale(), request.phoneNumber(),
                    request.streetAddress(), request.locality(), request.region(), request.postalCode(), request.country()
            ));
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (IllegalStateException exception) {
            throw new RestApiException(HttpStatus.CONFLICT, exception.getMessage());
        } catch (DataIntegrityViolationException exception) {
            throw new RestApiException(HttpStatus.CONFLICT, "Username or email is already in use");
        }
        return ResponseEntity.ok(toResponse(result));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changeCurrentPassword(@Valid @RequestBody ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "New password and confirmation password do not match");
        }
        UUID userUuid = getCurrentUserUseCase.execute().uuid();
        changePasswordUseCase.execute(new ChangePasswordCommand(userUuid, request.currentPassword(), request.newPassword()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/identities")
    public ResponseEntity<List<LinkedIdentityResponse>> getCurrentUserIdentities() {
        UUID userUuid = getCurrentUserUseCase.execute().uuid();
        List<LinkedIdentityResponse> identities = listUserIdentitiesUseCase.execute(userUuid)
                .stream()
                .map(this::toLinkedIdentityResponse)
                .toList();
        return ResponseEntity.ok(identities);
    }

    @PostMapping("/identities/link/{provider}")
    public ResponseEntity<IdentityLinkInitiationResponse> initiateIdentityLink(
            @PathVariable String provider,
            HttpServletRequest request
    ) {
        String registrationId = provider == null ? "" : provider.trim().toLowerCase(java.util.Locale.ROOT);
        try {
            IdentityProvider.valueOf(registrationId.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Unsupported identity provider");
        }
        UUID authenticatedUserUuid = getCurrentUserUseCase.execute().uuid();
        var session = request.getSession(true);
        session.removeAttribute(IdentityLinkSession.USER_UUID_ATTRIBUTE);
        session.setAttribute(IdentityLinkSession.USER_UUID_ATTRIBUTE, authenticatedUserUuid.toString());
        return ResponseEntity.ok(new IdentityLinkInitiationResponse(
                "/oauth2/authorization/" + registrationId));
    }

    @GetMapping("/login-history")
    public ResponseEntity<PageResponse<LoginEventResponse>> getCurrentUserLoginHistory(
            @org.springframework.web.bind.annotation.RequestParam(name = "outcome", required = false) LoginEventOutcome outcome,
            @org.springframework.web.bind.annotation.RequestParam(name = "page", defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(name = "size", defaultValue = "20") int size
    ) {
        UUID userUuid = getCurrentUserUseCase.execute().uuid();
        PageResult<LoginEvent> result;
        try {
            result = listLoginHistoryUseCase.execute(userUuid, outcome, page, size);
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        List<LoginEventResponse> content = result.content().stream().map(this::toLoginEventResponse).toList();
        return ResponseEntity.ok(new PageResponse<>(content, new PageMetadata(
                result.page(), result.size(), result.totalElements(), result.totalPages(),
                result.first(), result.last(), content.isEmpty())));
    }

    @GetMapping("/activity")
    public ResponseEntity<PageResponse<AccountActivityEventResponse>> getCurrentUserActivity(
            @org.springframework.web.bind.annotation.RequestParam(name = "page", defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(name = "size", defaultValue = "20") int size
    ) {
        UUID userUuid = getCurrentUserUseCase.execute().uuid();
        PageResult<AccountActivityEvent> result;
        try {
            result = listAccountActivityUseCase.execute(userUuid, page, size);
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        List<AccountActivityEventResponse> content = result.content().stream().map(this::toActivityResponse).toList();
        return ResponseEntity.ok(new PageResponse<>(content, new PageMetadata(result.page(), result.size(),
                result.totalElements(), result.totalPages(), result.first(), result.last(), content.isEmpty())));
    }

    @DeleteMapping("/identities/{identityUuid}")
    public ResponseEntity<Void> unlinkCurrentUserIdentity(@PathVariable UUID identityUuid) {
        UUID userUuid = getCurrentUserUseCase.execute().uuid();
        try {
            unlinkIdentityUseCase.execute(userUuid, identityUuid);
        } catch (IllegalStateException exception) {
            throw new RestApiException(HttpStatus.CONFLICT, exception.getMessage());
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/deactivate")
    public ResponseEntity<Void> deactivateCurrentUser() {
        UUID userUuid = getCurrentUserUseCase.execute().uuid();
        disableUserUseCase.execute(userUuid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private LinkedIdentityResponse toLinkedIdentityResponse(IdentityResult identity) {
        return new LinkedIdentityResponse(
                identity.uuid(),
                identity.provider(),
                identity.email(),
                identity.emailVerified(),
                identity.displayName(),
                identity.pictureUrl(),
                identity.linkedAt(),
                identity.lastLoginAt()
        );
    }

    private LoginEventResponse toLoginEventResponse(LoginEvent event) {
        return new LoginEventResponse(event.id(), event.userUuid(), event.outcome(), event.occurredAt(),
                event.ipAddress(), event.userAgent(), event.failureCategory());
    }

    private AccountActivityEventResponse toActivityResponse(AccountActivityEvent event) {
        return new AccountActivityEventResponse(event.id(), event.eventType(), event.description(),
                event.occurredAt(), event.ipAddress(), event.userAgent());
    }

    private UserResponse toResponse(UserResult result) {
        return new UserResponse(
                result.uuid(), result.username(), result.email(), result.givenName(), result.familyName(), result.gender(),
                null, result.phoneNumber(), null, null, null, result.enabled(), result.accountLocked(),
                result.createdAt(), result.updatedAt(), result.pictureUrl(), result.middleName(), result.nickname(),
                result.emailVerified(), result.phoneNumberVerified()
        );
    }
}
