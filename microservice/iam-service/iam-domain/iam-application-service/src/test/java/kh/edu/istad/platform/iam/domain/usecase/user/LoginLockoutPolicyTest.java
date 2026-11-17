package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.result.LoginAttemptUpdate;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.domain.port.out.SystemConfigurationRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.usecase.audit.RecordAuditLogUseCase;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginLockoutPolicyTest {
    private final UserRepository users = mock(UserRepository.class);
    private final SystemConfigurationRepository configuration = mock(SystemConfigurationRepository.class);
    private final UserSessionRepository sessions = mock(UserSessionRepository.class);
    private final RecordAccountActivityEventUseCase activity = mock(RecordAccountActivityEventUseCase.class);
    private final RecordAuditLogUseCase audit = mock(RecordAuditLogUseCase.class);

    @Test
    void failuresBelowThresholdDoNotLockOrRevokeSessions() {
        UUID uuid = UUID.randomUUID();
        when(users.findByUsernameOrEmail("alice")).thenReturn(Optional.of(user(uuid)));
        when(configuration.findCurrent()).thenReturn(new IamSystemConfiguration(true, 8, 3));
        when(users.recordFailedLoginAttempt(uuid, 3)).thenReturn(new LoginAttemptUpdate(2, false, true));

        new RecordLoginAttemptUseCase(users, configuration, sessions, activity, audit).execute("alice", false);

        verifyNoInteractions(sessions, activity, audit);
    }

    @Test
    void reachingThresholdLocksRevokesSessionsAndRecordsSafeActivityAndAudit() {
        UUID uuid = UUID.randomUUID();
        when(users.findByUsernameOrEmail("alice")).thenReturn(Optional.of(user(uuid)));
        when(configuration.findCurrent()).thenReturn(new IamSystemConfiguration(true, 8, 3));
        when(users.recordFailedLoginAttempt(uuid, 3)).thenReturn(new LoginAttemptUpdate(3, true, true));

        new RecordLoginAttemptUseCase(users, configuration, sessions, activity, audit).execute("alice", false);

        verify(sessions).revokeAll(eq(uuid), isNull(), any(), eq("ACCOUNT_LOCKED"));
        verify(activity).execute(uuid, AccountActivityEventType.ACCOUNT_LOCKED,
                "Account automatically locked after reaching the failed password login limit");
        verify(audit).record(eq("ACCOUNT_AUTO_LOCKED"), eq("USER"), eq(uuid.toString()), any(), isNull(),
                contains("FAILED_PASSWORD_LOGIN_THRESHOLD"));
    }

    @Test
    void successfulAuthenticationUsesTheAtomicResetPathWithoutUnlocking() {
        UUID uuid = UUID.randomUUID();
        when(users.findByUsernameOrEmail("alice")).thenReturn(Optional.of(user(uuid)));

        new RecordLoginAttemptUseCase(users, configuration, sessions, activity, audit).execute("alice", true);

        verify(users).recordSuccessfulLogin(eq(uuid), any());
        verifyNoInteractions(configuration, sessions, activity, audit);
    }

    private User user(UUID uuid) {
        User user = new User(); user.setUuid(uuid); return user;
    }
}
