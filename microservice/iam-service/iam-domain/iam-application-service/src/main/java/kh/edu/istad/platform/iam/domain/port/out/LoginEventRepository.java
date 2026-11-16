package kh.edu.istad.platform.iam.domain.port.out;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.LoginEvent;
import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;

import java.util.UUID;

public interface LoginEventRepository {
    LoginEvent save(LoginEvent event);

    PageResult<LoginEvent> findHistory(UUID userUuid, LoginEventOutcome outcome, int page, int size);
}
