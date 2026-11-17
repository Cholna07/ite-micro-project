package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.LoginEvent;
import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;
import kh.edu.istad.platform.iam.domain.port.out.LoginEventRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class RecordLoginEventUseCase {
    private final UserRepository userRepository;
    private final LoginEventRepository loginEventRepository;
    private final RecordLoginAttemptUseCase recordLoginAttemptUseCase;

    public RecordLoginEventUseCase(
            UserRepository userRepository,
            LoginEventRepository loginEventRepository,
            RecordLoginAttemptUseCase recordLoginAttemptUseCase
    ) {
        this.userRepository = userRepository;
        this.loginEventRepository = loginEventRepository;
        this.recordLoginAttemptUseCase = recordLoginAttemptUseCase;
    }

    public void execute(String usernameOrEmail, LoginEventOutcome outcome, String ipAddress,
                        String userAgent, String failureCategory) {
        var user = usernameOrEmail == null || usernameOrEmail.isBlank()
                ? java.util.Optional.<kh.edu.istad.platform.iam.domain.entity.User>empty()
                : userRepository.findByUsernameOrEmail(usernameOrEmail.trim());

        LoginEvent event = new LoginEvent(
                UUID.randomUUID(),
                user.map(kh.edu.istad.platform.iam.domain.entity.User::getId).orElse(null),
                user.map(kh.edu.istad.platform.iam.domain.entity.User::getUuid).orElse(null),
                outcome,
                Instant.now(),
                truncate(ipAddress, 45),
                truncate(userAgent, 512),
                outcome == LoginEventOutcome.SUCCESS ? null : truncate(failureCategory, 50)
        );
        loginEventRepository.save(event);

        // Preserve the event even if updating the legacy aggregate counters fails.
        if (user.isPresent() && outcome == LoginEventOutcome.SUCCESS) {
            recordLoginAttemptUseCase.execute(usernameOrEmail, true);
        } else if (user.isPresent() && outcome == LoginEventOutcome.FAILURE
                && "INVALID_CREDENTIALS".equals(failureCategory)) {
            recordLoginAttemptUseCase.execute(usernameOrEmail, false);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.isBlank()) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
