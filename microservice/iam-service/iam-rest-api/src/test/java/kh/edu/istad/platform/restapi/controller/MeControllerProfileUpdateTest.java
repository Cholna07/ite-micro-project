package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.command.UpdateUserProfileCommand;
import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEvent;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.usecase.identity.ListUserIdentitiesUseCase;
import kh.edu.istad.platform.iam.domain.usecase.identity.UnlinkIdentityUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ChangePasswordUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.DisableUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.GetCurrentUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListLoginHistoryUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListAccountActivityUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.UpdateUserProfileUseCase;
import kh.edu.istad.platform.restapi.dto.request.UpdateUserProfileRequest;
import kh.edu.istad.platform.restapi.exception.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeControllerProfileUpdateTest {
    @Mock GetCurrentUserUseCase currentUser;
    @Mock ListUserIdentitiesUseCase listIdentities;
    @Mock UnlinkIdentityUseCase unlinkIdentity;
    @Mock DisableUserUseCase disableUser;
    @Mock UpdateUserProfileUseCase updateProfile;
    @Mock ChangePasswordUseCase changePassword;
    @Mock ListLoginHistoryUseCase loginHistory;
    @Mock ListAccountActivityUseCase activity;
    @InjectMocks MeController controller;

    @Test
    void profileUpdateUsesTheAuthenticatedUserUuidAndForwardsUsernameAndEmail() {
        UUID authenticatedUserUuid = UUID.randomUUID();
        when(currentUser.execute()).thenReturn(userResult(authenticatedUserUuid));
        when(updateProfile.execute(any())).thenReturn(userResult(authenticatedUserUuid));

        controller.updateCurrentUser(request("new-name", "new@example.com"));

        ArgumentCaptor<UpdateUserProfileCommand> command = ArgumentCaptor.forClass(UpdateUserProfileCommand.class);
        verify(updateProfile).execute(command.capture());
        assertEquals(authenticatedUserUuid, command.getValue().userUuid());
        assertEquals("new-name", command.getValue().username());
        assertEquals("new@example.com", command.getValue().email());
    }

    @Test
    void mapsDuplicateProfileValueToConflictWithTheBackendMessage() {
        UUID authenticatedUserUuid = UUID.randomUUID();
        when(currentUser.execute()).thenReturn(userResult(authenticatedUserUuid));
        when(updateProfile.execute(any())).thenThrow(new IllegalStateException("Email is already in use"));

        RestApiException exception = assertThrows(RestApiException.class,
                () -> controller.updateCurrentUser(request("name", "taken@example.com")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Email is already in use", exception.getMessage());
    }

    @Test
    void linkingInitiationStoresOnlyTheAuthenticatedAccountAsCallbackBinding() {
        UUID authenticatedUuid = UUID.randomUUID();
        when(currentUser.execute()).thenReturn(userResult(authenticatedUuid));
        MockHttpServletRequest request = new MockHttpServletRequest();

        var response = controller.initiateIdentityLink("google", request);

        assertEquals("/oauth2/authorization/google", response.getBody().authorizationUrl());
        assertEquals(authenticatedUuid.toString(), request.getSession().getAttribute(
                kh.edu.istad.platform.iam.domain.usecase.identity.IdentityLinkSession.USER_UUID_ATTRIBUTE));
    }

    @Test
    void accountActivityUsesOnlyTheAuthenticatedUsersUuidAndForwardsPagination() {
        UUID authenticatedUuid = UUID.randomUUID();
        when(currentUser.execute()).thenReturn(userResult(authenticatedUuid));
        AccountActivityEvent event = new AccountActivityEvent(UUID.randomUUID(), authenticatedUuid,
                AccountActivityEventType.PROFILE_UPDATED, "Profile information updated",
                java.time.Instant.now(), "127.0.0.1", "Browser");
        when(activity.execute(authenticatedUuid, 2, 10)).thenReturn(
                new PageResult<>(java.util.List.of(event), 2, 10, 21, 3, false, true));

        var response = controller.getCurrentUserActivity(2, 10);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(event.id(), response.getBody().content().get(0).id());
        verify(activity).execute(authenticatedUuid, 2, 10);
        verifyNoMoreInteractions(activity);
    }

    private UpdateUserProfileRequest request(String username, String email) {
        return new UpdateUserProfileRequest(username, email, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private UserResult userResult(UUID uuid) {
        return new UserResult(uuid, "local-user", null, null, null, null, null, null, null,
                false, false, true, false, null, null);
    }
}
