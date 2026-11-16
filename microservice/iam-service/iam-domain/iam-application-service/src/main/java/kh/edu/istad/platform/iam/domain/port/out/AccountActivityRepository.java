package kh.edu.istad.platform.iam.domain.port.out;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEvent;

import java.util.UUID;

public interface AccountActivityRepository {
    AccountActivityEvent save(AccountActivityEvent event);

    PageResult<AccountActivityEvent> findByUserUuid(UUID userUuid, int page, int size);

    PageResult<AccountActivityEvent> findAll(int page, int size);
}
