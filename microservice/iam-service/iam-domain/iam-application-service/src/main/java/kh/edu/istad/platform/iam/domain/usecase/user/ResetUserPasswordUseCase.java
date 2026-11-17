package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.ResetUserPasswordCommand;
import kh.edu.istad.platform.iam.domain.audit.AuditAction;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.usecase.config.PasswordPolicy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
@Component
public class ResetUserPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final CurrentActorProvider currentActorProvider;
    private final RecordAccountActivityEventUseCase recordActivity;
    private final UserSessionRepository userSessionRepository;
    private final PasswordPolicy passwordPolicy;

    public ResetUserPasswordUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            CurrentActorProvider currentActorProvider,
            RecordAccountActivityEventUseCase recordActivity,
            UserSessionRepository userSessionRepository,
            PasswordPolicy passwordPolicy
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.currentActorProvider = currentActorProvider;
        this.recordActivity = recordActivity;
        this.userSessionRepository = userSessionRepository;
        this.passwordPolicy = passwordPolicy;
    }

    @Transactional
    @AuditAction(action = "USER_PASSWORD_RESET", targetType = "USER", targetArgumentIndex = 0, targetProperty = "userUuid")
    public void execute(ResetUserPasswordCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("Reset password command cannot be null");
        }

        if (command.newPassword() == null || command.newPassword().isBlank()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        passwordPolicy.validate(command.newPassword());

        User user = userRepository.findByUuid(command.userUuid())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + command.userUuid()));

        user.setPasswordHash(passwordHasher.hash(command.newPassword()));
        user.setPasswordChangedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(currentActorProvider.getCurrentActor());

        userRepository.save(user);
        userSessionRepository.revokeAll(user.getUuid(), null, Instant.now(), "PASSWORD_RESET");
        recordActivity.execute(user.getUuid(), AccountActivityEventType.PASSWORD_CHANGED,
                "Password reset by an administrator; all active sessions were revoked");
    }
}
