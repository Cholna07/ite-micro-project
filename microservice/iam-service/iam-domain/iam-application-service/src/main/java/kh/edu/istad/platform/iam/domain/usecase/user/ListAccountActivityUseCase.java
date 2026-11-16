package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEvent;
import kh.edu.istad.platform.iam.domain.port.out.AccountActivityRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ListAccountActivityUseCase {
    private final AccountActivityRepository repository;

    public ListAccountActivityUseCase(AccountActivityRepository repository) {
        this.repository = repository;
    }

    public PageResult<AccountActivityEvent> execute(UUID userUuid, int page, int size) {
        if (userUuid == null) throw new IllegalArgumentException("User UUID is required");
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
        return repository.findByUserUuid(userUuid, page, size);
    }

    public PageResult<AccountActivityEvent> executeAll(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
        return repository.findAll(page, size);
    }
}
