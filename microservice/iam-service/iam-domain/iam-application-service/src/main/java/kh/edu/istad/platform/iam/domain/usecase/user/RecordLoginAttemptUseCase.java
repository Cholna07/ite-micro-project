package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.SystemConfigurationRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.usecase.audit.RecordAuditLogUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
@Component
public class RecordLoginAttemptUseCase {

    private final UserRepository userRepository;
    private final SystemConfigurationRepository configurationRepository;
    private final UserSessionRepository sessions;
    private final RecordAccountActivityEventUseCase accountActivity;
    private final RecordAuditLogUseCase auditLog;

    public RecordLoginAttemptUseCase(UserRepository userRepository,
                                     SystemConfigurationRepository configurationRepository,
                                     UserSessionRepository sessions,
                                     RecordAccountActivityEventUseCase accountActivity,
                                     RecordAuditLogUseCase auditLog) {
        this.userRepository = userRepository;
        this.configurationRepository = configurationRepository;
        this.sessions = sessions;
        this.accountActivity = accountActivity;
        this.auditLog = auditLog;
    }

    @Transactional
    public boolean execute(String usernameOrEmail, boolean success) {

        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Username or email is required"
            );
        }

        User user = userRepository
                .findByUsernameOrEmail(usernameOrEmail.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (success) {
            userRepository.recordSuccessfulLogin(user.getUuid(), Instant.now());
            return false;
        }

        int threshold = configurationRepository.findCurrent().maxFailedLoginAttempts();
        var update = userRepository.recordFailedLoginAttempt(user.getUuid(), threshold);
        if (!update.changed() || !update.locked()) return false;

        Instant now = Instant.now();
        sessions.revokeAll(user.getUuid(), null, now, "ACCOUNT_LOCKED");
        accountActivity.execute(user.getUuid(), AccountActivityEventType.ACCOUNT_LOCKED,
                "Account automatically locked after reaching the failed password login limit");
        auditLog.record("ACCOUNT_AUTO_LOCKED", "USER", user.getUuid().toString(),
                kh.edu.istad.platform.iam.domain.entity.AuditLogOutcome.SUCCESS, null,
                "{\"reason\":\"FAILED_PASSWORD_LOGIN_THRESHOLD\",\"threshold\":" + threshold + "}");
        return true;
    }
}
