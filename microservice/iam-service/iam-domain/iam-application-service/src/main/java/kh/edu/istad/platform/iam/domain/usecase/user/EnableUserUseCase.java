package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class EnableUserUseCase {

    private final UserRepository userRepository;
    private final CurrentActorProvider currentActorProvider;

    public EnableUserUseCase(
            UserRepository userRepository,
            CurrentActorProvider currentActorProvider
    ) {
        this.userRepository = userRepository;
        this.currentActorProvider = currentActorProvider;
    }

    public void execute(UUID userUuid) {

        if (userUuid == null) {
            throw new IllegalArgumentException("User UUID cannot be null");
        }

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + userUuid));

        user.setEnabled(true);
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(currentActorProvider.getCurrentActor());

        userRepository.save(user);
    }
}