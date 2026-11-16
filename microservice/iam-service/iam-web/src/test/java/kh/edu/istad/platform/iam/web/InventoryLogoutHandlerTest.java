package kh.edu.istad.platform.iam.web.config;

import kh.edu.istad.platform.iam.domain.entity.SessionCredentialType;
import kh.edu.istad.platform.iam.domain.entity.UserSession;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.HttpSessionInventory;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryLogoutHandlerTest {
    @Mock UserSessionRepository sessions;
    @Mock RecordAccountActivityEventUseCase recordActivity;

    @Test
    void revokesTheInventoryRecordAssociatedWithTheServerSession() {
        UUID sessionId = UUID.randomUUID();
        UUID ownerUuid = UUID.randomUUID();
        Instant now = Instant.now();
        UserSession record = new UserSession(sessionId, ownerUuid, SessionCredentialType.HTTP_SESSION,
                "SOCIAL", "google", now.minusSeconds(10), now, now.plusSeconds(600), null, null,
                null, null, null);
        when(sessions.findById(sessionId)).thenReturn(Optional.of(record));
        when(sessions.revoke(eq(sessionId), eq(ownerUuid), eq(ownerUuid), any(), eq("LOGOUT"))).thenReturn(true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(HttpSessionInventory.SESSION_RECORD_ID, sessionId.toString());

        new InventoryLogoutHandler(sessions, recordActivity).logout(request, new MockHttpServletResponse(), null);

        verify(sessions).revoke(eq(sessionId), eq(ownerUuid), eq(ownerUuid), any(), eq("LOGOUT"));
        verify(recordActivity).execute(eq(ownerUuid), eq(kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType.CURRENT_SESSION_REVOKED), anyString());
    }

    @Test
    void ignoresAnAbsentInventoryMarkerWithoutCreatingASession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        new InventoryLogoutHandler(sessions, recordActivity).logout(request, new MockHttpServletResponse(), null);
        verifyNoInteractions(sessions);
        org.junit.jupiter.api.Assertions.assertNull(request.getSession(false));
    }
}
