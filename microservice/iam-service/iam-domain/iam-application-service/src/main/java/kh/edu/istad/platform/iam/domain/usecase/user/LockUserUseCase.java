package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.audit.AuditAction;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class LockUserUseCase {

    private final UserRepository userRepository;
    private final CurrentActorProvider currentActorProvider;
    private final UserSessionRepository userSessionRepository;
    private final RecordAccountActivityEventUseCase recordActivity;

    public LockUserUseCase(
            UserRepository userRepository,
            CurrentActorProvider currentActorProvider,
            UserSessionRepository userSessionRepository,
            RecordAccountActivityEventUseCase recordActivity
    ) {
        this.userRepository = userRepository;
        this.currentActorProvider = currentActorProvider;
        this.userSessionRepository = userSessionRepository;
        this.recordActivity = recordActivity;
    }

    @Transactional
    @AuditAction(action = "USER_LOCKED", targetType = "USER", targetArgumentIndex = 0)
    public void execute(UUID userUuid) {

        if (userUuid == null) {
            throw new IllegalArgumentException("User UUID cannot be null");
        }

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + userUuid));

        user.setAccountNonLocked(false);
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(currentActorProvider.getCurrentActor());

        userRepository.save(user);
        userSessionRepository.revokeAll(userUuid, null, Instant.now(), "ACCOUNT_LOCKED");
        recordActivity.execute(userUuid, AccountActivityEventType.ACCOUNT_LOCKED,
                "Account locked; all active sessions were revoked");
    }
}
