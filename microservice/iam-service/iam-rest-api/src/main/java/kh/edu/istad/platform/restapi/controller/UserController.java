package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.domain.dto.command.ChangePasswordCommand;
import kh.edu.istad.platform.iam.domain.dto.command.AssignRolesToUserCommand;
import kh.edu.istad.platform.iam.domain.dto.command.GrantPermissionsToUserCommand;
import kh.edu.istad.platform.iam.domain.dto.command.CreateUserCommand;
import kh.edu.istad.platform.iam.domain.dto.command.ResetUserPasswordCommand;
import kh.edu.istad.platform.iam.domain.dto.command.UpdateUserProfileCommand;
import kh.edu.istad.platform.iam.domain.dto.query.UserFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.dto.result.UserSummary;
import kh.edu.istad.platform.iam.domain.dto.result.RoleAssignmentResult;
import kh.edu.istad.platform.iam.domain.dto.result.PermissionGrantResult;
import kh.edu.istad.platform.iam.domain.dto.result.UserRoleAssignmentView;
import kh.edu.istad.platform.iam.domain.dto.result.UserPermissionAssignmentView;
import kh.edu.istad.platform.iam.domain.exception.RoleAlreadyAssignedException;
import kh.edu.istad.platform.iam.domain.exception.PermissionAlreadyAssignedException;
import kh.edu.istad.platform.iam.domain.usecase.user.ChangePasswordUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.AssignRolesToUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.GrantPermissionsToUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RevokeRoleFromUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RevokePermissionFromUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListUserRoleAssignmentsUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListUserPermissionAssignmentsUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.CreateUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.DeleteUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.DisableUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.EnableUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.GetUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListUsersUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.LockUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ResetUserPasswordUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.UnlockUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.UpdateUserProfileUseCase;
import kh.edu.istad.platform.restapi.dto.request.ChangePasswordRequest;
import kh.edu.istad.platform.restapi.dto.request.AssignRolesToUserRequest;
import kh.edu.istad.platform.restapi.dto.request.GrantPermissionsToUserRequest;
import kh.edu.istad.platform.restapi.dto.request.CreateUserRequest;
import kh.edu.istad.platform.restapi.dto.request.ResetUserPasswordRequest;
import kh.edu.istad.platform.restapi.dto.request.UpdateUserProfileRequest;
import kh.edu.istad.platform.restapi.dto.response.PageMetadata;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import kh.edu.istad.platform.restapi.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import kh.edu.istad.platform.iam.domain.dto.result.UserAuthoritiesResult;
import kh.edu.istad.platform.iam.domain.usecase.user.GetUserAuthoritiesUseCase;
import kh.edu.istad.platform.restapi.dto.response.UserAuthoritiesResponse;

import java.util.Set;
import java.util.List;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final ResetUserPasswordUseCase resetUserPasswordUseCase;
    private final EnableUserUseCase enableUserUseCase;
    private final DisableUserUseCase disableUserUseCase;
    private final LockUserUseCase lockUserUseCase;
    private final UnlockUserUseCase unlockUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetUserAuthoritiesUseCase getUserAuthoritiesUseCase;
    private final AssignRolesToUserUseCase assignRolesToUserUseCase;
    private final RevokeRoleFromUserUseCase revokeRoleFromUserUseCase;
    private final GrantPermissionsToUserUseCase grantPermissionsToUserUseCase;
    private final RevokePermissionFromUserUseCase revokePermissionFromUserUseCase;
    private final ListUserRoleAssignmentsUseCase listUserRoleAssignmentsUseCase;
    private final ListUserPermissionAssignmentsUseCase listUserPermissionAssignmentsUseCase;

    // POST /api/users
    @PostMapping
    public ResponseEntity<UserResult> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        CreateUserCommand command = new CreateUserCommand(
                request.username(),
                request.email(),
                request.password(),
                request.givenName(),
                request.familyName(),
                request.middleName(),
                request.phoneNumber()
        );

        UserResult result = createUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // GET /api/users
    @GetMapping
    public ResponseEntity<PageResponse<UserSummary>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean emailVerified,
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(required = false) Boolean locked,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 100");
        }
        UserFilterQuery query = new UserFilterQuery(
                keyword,
                enabled,
                emailVerified,
                deleted,
                locked,
                page,
                size,
                sortBy,
                sortDirection
        );

        PageResult<UserSummary> result = listUsersUseCase.execute(query);

        PageMetadata metadata = new PageMetadata(
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.first(),
                result.last(),
                result.totalElements() == 0
        );

        PageResponse<UserSummary> response = new PageResponse<>(
                result.content(),
                metadata
        );

        return ResponseEntity.ok(response);
    }

    // GET /api/users/{uuid}
    @GetMapping("/{uuid}")
    public ResponseEntity<UserResult> getUser(@PathVariable UUID uuid) {
        UserResult result = getUserUseCase.execute(uuid);
        return ResponseEntity.ok(result);
    }

    // PUT /api/users/{uuid}
    @PutMapping("/{uuid}")
    public ResponseEntity<UserResult> updateUserProfile(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                uuid,
                request.username(),
                request.email(),
                request.givenName(),
                request.familyName(),
                request.middleName(),
                request.nickname(),
                request.gender(),
                request.birthdate(),
                request.profileUrl(),
                request.pictureUrl(),
                request.websiteUrl(),
                request.zoneInfo(),
                request.locale(),
                request.phoneNumber(),
                request.streetAddress(),
                request.locality(),
                request.region(),
                request.postalCode(),
                request.country()
        );

        UserResult result = updateUserProfileUseCase.execute(command);
        return ResponseEntity.ok(result);
    }

    // PUT /api/users/{uuid}/password
    @PutMapping("/{uuid}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID uuid,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new RestApiException(
                    HttpStatus.BAD_REQUEST,
                    "New password and confirmation password do not match"
            );
        }

        ChangePasswordCommand command = new ChangePasswordCommand(
                uuid,
                request.currentPassword(),
                request.newPassword()
        );

        changePasswordUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/users/{uuid}/password/reset
    @PutMapping("/{uuid}/password/reset")
    public ResponseEntity<Void> resetUserPassword(
            @PathVariable UUID uuid,
            @Valid @RequestBody ResetUserPasswordRequest request
    ) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new RestApiException(
                    HttpStatus.BAD_REQUEST,
                    "New password and confirmation password do not match"
            );
        }

        ResetUserPasswordCommand command = new ResetUserPasswordCommand(
                uuid,
                request.newPassword()
        );

        resetUserPasswordUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/users/{uuid}/enable
    @PutMapping("/{uuid}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable UUID uuid) {
        enableUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/users/{uuid}/disable
    @PutMapping("/{uuid}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable UUID uuid) {
        disableUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/users/{uuid}/lock
    @PutMapping("/{uuid}/lock")
    public ResponseEntity<Void> lockUser(@PathVariable UUID uuid) {
        lockUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/users/{uuid}/unlock
    @PutMapping("/{uuid}/unlock")
    public ResponseEntity<Void> unlockUser(@PathVariable UUID uuid) {
        unlockUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/users/{uuid}
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID uuid) {
        deleteUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }
    // GET /api/users/{uuid}/authorities
    @GetMapping("/{uuid}/authorities")
    public ResponseEntity<UserAuthoritiesResponse> getUserAuthorities(
            @PathVariable UUID uuid
    ) {
        UserAuthoritiesResult result = getUserAuthoritiesUseCase.execute(uuid);

        UserResult user = getUserUseCase.execute(uuid);

        UserAuthoritiesResponse response = new UserAuthoritiesResponse(
                result.userUuid(),
                user.username(),
                Set.copyOf(result.roles()),
                Set.copyOf(result.permissions())
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{uuid}/roles")
    public ResponseEntity<List<RoleAssignmentResult>> assignRoles(
            @PathVariable UUID uuid,
            @Valid @RequestBody AssignRolesToUserRequest request
    ) {
        try {
            List<RoleAssignmentResult> result = assignRolesToUserUseCase.execute(
                    new AssignRolesToUserCommand(uuid, request.roleUuids(), request.expiresAt(), request.reason())
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RoleAlreadyAssignedException exception) {
            throw new RestApiException(HttpStatus.CONFLICT, exception.getMessage());
        } catch (DataIntegrityViolationException exception) {
            // Covers a concurrent request that wins the database uniqueness race.
            throw new RestApiException(HttpStatus.CONFLICT, "One or more roles are already assigned to this user");
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @GetMapping("/{uuid}/roles/assignments")
    public ResponseEntity<List<UserRoleAssignmentView>> listRoleAssignments(@PathVariable UUID uuid) {
        return ResponseEntity.ok(listUserRoleAssignmentsUseCase.execute(uuid));
    }

    @DeleteMapping("/{uuid}/roles/{roleUuid}")
    public ResponseEntity<Void> revokeRole(
            @PathVariable UUID uuid,
            @PathVariable UUID roleUuid,
            @RequestParam(required = false) String reason
    ) {
        revokeRoleFromUserUseCase.execute(uuid, roleUuid, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/permissions")
    public ResponseEntity<List<PermissionGrantResult>> grantPermissions(
            @PathVariable UUID uuid,
            @Valid @RequestBody GrantPermissionsToUserRequest request
    ) {
        try {
            List<PermissionGrantResult> result = grantPermissionsToUserUseCase.execute(
                    new GrantPermissionsToUserCommand(uuid, request.permissionUuids(), request.effect(), request.expiresAt(), request.reason())
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (PermissionAlreadyAssignedException exception) {
            throw new RestApiException(HttpStatus.CONFLICT, exception.getMessage());
        } catch (DataIntegrityViolationException exception) {
            throw new RestApiException(HttpStatus.CONFLICT,
                    "One or more permissions are already assigned to this user");
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (IllegalStateException exception) {
            throw new RestApiException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    @GetMapping("/{uuid}/permissions/direct")
    public ResponseEntity<List<UserPermissionAssignmentView>> listDirectPermissionAssignments(@PathVariable UUID uuid) {
        return ResponseEntity.ok(listUserPermissionAssignmentsUseCase.execute(uuid));
    }

    @DeleteMapping("/{uuid}/permissions/{permissionUuid}")
    public ResponseEntity<Void> revokePermission(
            @PathVariable UUID uuid,
            @PathVariable UUID permissionUuid,
            @RequestParam(required = false) String reason
    ) {
        revokePermissionFromUserUseCase.execute(uuid, permissionUuid, reason);
        return ResponseEntity.noContent().build();
    }

}
