package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.usecase.user.GetCurrentUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListUserSessionsUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RevokeUserSessionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.HttpSessionInventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionActivityControllerTest {
    @Mock GetCurrentUserUseCase currentUser;
    @Mock ListUserSessionsUseCase listSessions;
    @Mock RevokeUserSessionUseCase revokeSession;
    @Mock RecordAccountActivityEventUseCase recordActivity;

    @Test
    void recordsCurrentSessionRevocationOnlyAfterItSucceeds() {
        UUID userUuid = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(currentUser.execute()).thenReturn(user(userUuid));
        when(revokeSession.execute(userUuid, sessionId, userUuid, "LOGOUT")).thenReturn(true);
        MockHttpServletRequest request = request(sessionId);

        new SessionController(currentUser, listSessions, revokeSession, recordActivity).revokeCurrent(request);

        verify(recordActivity).execute(userUuid, AccountActivityEventType.CURRENT_SESSION_REVOKED,
                "Current session revoked");
    }

    @Test
    void recordsOtherSessionRevocationOnlyWhenAnySessionWasRevoked() {
        UUID userUuid = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(currentUser.execute()).thenReturn(user(userUuid));
        when(revokeSession.revokeOthers(userUuid, sessionId, userUuid)).thenReturn(2);

        new SessionController(currentUser, listSessions, revokeSession, recordActivity).revokeOthers(request(sessionId));

        verify(recordActivity).execute(userUuid, AccountActivityEventType.OTHER_SESSIONS_REVOKED,
                "Other active sessions revoked");
    }

    @Test
    void failedSessionRevocationDoesNotRecordACompletedEvent() {
        UUID userUuid = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(currentUser.execute()).thenReturn(user(userUuid));
        when(revokeSession.execute(userUuid, sessionId, userUuid, "LOGOUT")).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(kh.edu.istad.platform.restapi.exception.RestApiException.class,
                () -> new SessionController(currentUser, listSessions, revokeSession, recordActivity)
                        .revokeCurrent(request(sessionId)));
        verifyNoInteractions(recordActivity);
    }

    private MockHttpServletRequest request(UUID sessionId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(HttpSessionInventory.SESSION_RECORD_ID, sessionId.toString());
        return request;
    }

    private UserResult user(UUID uuid) {
        return new UserResult(uuid, "user", null, null, null, null, null, null, null,
                false, false, true, false, null, null);
    }
}
