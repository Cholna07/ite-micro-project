package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.ResetUserPasswordCommand;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
@Component
public class ResetUserPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final CurrentActorProvider currentActorProvider;

    public ResetUserPasswordUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            CurrentActorProvider currentActorProvider
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.currentActorProvider = currentActorProvider;
    }

    public void execute(ResetUserPasswordCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("Reset password command cannot be null");
        }

        if (command.newPassword() == null || command.newPassword().isBlank()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        User user = userRepository.findByUuid(command.userUuid())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + command.userUuid()));

        user.setPasswordHash(passwordHasher.hash(command.newPassword()));
        user.setPasswordChangedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(currentActorProvider.getCurrentActor());

        userRepository.save(user);
    }
}
