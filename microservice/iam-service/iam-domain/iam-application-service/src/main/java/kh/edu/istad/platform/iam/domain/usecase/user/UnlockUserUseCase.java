package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.audit.AuditAction;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
@Component
public class UnlockUserUseCase {

    private final UserRepository userRepository;
    private final CurrentActorProvider currentActorProvider;
    private final RecordAccountActivityEventUseCase recordActivity;

    public UnlockUserUseCase(
            UserRepository userRepository,
            CurrentActorProvider currentActorProvider,
            RecordAccountActivityEventUseCase recordActivity
    ) {
        this.userRepository = userRepository;
        this.currentActorProvider = currentActorProvider;
        this.recordActivity = recordActivity;
    }

    @Transactional
    @AuditAction(action = "USER_UNLOCKED", targetType = "USER", targetArgumentIndex = 0)
    public void execute(UUID userUuid) {

        if (userUuid == null) {
            throw new IllegalArgumentException("User UUID cannot be null");
        }

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + userUuid));

        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(currentActorProvider.getCurrentActor());

        userRepository.save(user);
        recordActivity.execute(userUuid, AccountActivityEventType.ACCOUNT_UNLOCKED,
                "Account unlocked; failed login counter reset");
    }
}
