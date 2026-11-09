package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
@Component
public class RecordLoginAttemptUseCase {

    private final UserRepository userRepository;

    public RecordLoginAttemptUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(String usernameOrEmail, boolean success) {

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
            user.setFailedLoginAttempts(0);
            user.setLastLoginAt(Instant.now());
        } else {
            user.setFailedLoginAttempts(
                    user.getFailedLoginAttempts() + 1
            );
        }

        userRepository.save(user);
    }
}
