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
public class DisableUserUseCase {

    private final UserRepository userRepository;
    private final CurrentActorProvider currentActorProvider;
    private final UserSessionRepository userSessionRepository;
    private final RecordAccountActivityEventUseCase recordActivity;

    public DisableUserUseCase(
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
    @AuditAction(action = "USER_DISABLED", targetType = "USER", targetArgumentIndex = 0)
    public void execute(UUID userUuid) {

        if (userUuid == null) {
            throw new IllegalArgumentException("User UUID cannot be null");
        }

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + userUuid));

        String actor = currentActorProvider.getCurrentActor();
        user.setEnabled(false);
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(actor);

        userRepository.save(user);
        userSessionRepository.revokeAll(userUuid, resolveActorUuid(actor), Instant.now(), "ACCOUNT_DEACTIVATED");
        recordActivity.execute(userUuid, AccountActivityEventType.ACCOUNT_DEACTIVATED,
                "Account deactivated; all active sessions were revoked");
    }

    private UUID resolveActorUuid(String actor) {
        if (actor == null || actor.isBlank() || "SYSTEM".equals(actor)) return null;
        try {
            return UUID.fromString(actor);
        } catch (IllegalArgumentException ignored) {
            return userRepository.findByUsernameOrEmail(actor).map(user -> user.getUuid()).orElse(null);
        }
    }
}
