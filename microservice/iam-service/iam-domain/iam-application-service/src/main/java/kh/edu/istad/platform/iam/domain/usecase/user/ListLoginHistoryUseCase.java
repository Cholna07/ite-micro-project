package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.LoginEvent;
import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;
import kh.edu.istad.platform.iam.domain.port.out.LoginEventRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ListLoginHistoryUseCase {
    private final LoginEventRepository loginEventRepository;

    public ListLoginHistoryUseCase(LoginEventRepository loginEventRepository) {
        this.loginEventRepository = loginEventRepository;
    }

    public PageResult<LoginEvent> execute(UUID userUuid, LoginEventOutcome outcome, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
        return loginEventRepository.findHistory(userUuid, outcome, page, size);
    }
}
