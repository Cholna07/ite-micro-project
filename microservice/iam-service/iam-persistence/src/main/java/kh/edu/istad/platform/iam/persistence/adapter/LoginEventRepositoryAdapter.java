package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.LoginEvent;
import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;
import kh.edu.istad.platform.iam.domain.port.out.LoginEventRepository;
import kh.edu.istad.platform.iam.persistence.entity.LoginEventJpaEntity;
import kh.edu.istad.platform.iam.persistence.repository.LoginEventJpaRepository;
import kh.edu.istad.platform.iam.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class LoginEventRepositoryAdapter implements LoginEventRepository {
    private final LoginEventJpaRepository loginEventJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public LoginEvent save(LoginEvent event) {
        LoginEventJpaEntity entity = new LoginEventJpaEntity();
        entity.setId(event.id());
        if (event.userId() != null) {
            entity.setUser(userJpaRepository.getReferenceById(event.userId()));
        }
        entity.setOutcome(event.outcome());
        entity.setOccurredAt(event.occurredAt());
        entity.setIpAddress(event.ipAddress());
        entity.setUserAgent(event.userAgent());
        entity.setFailureCategory(event.failureCategory());
        return toDomain(loginEventJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<LoginEvent> findHistory(UUID userUuid, LoginEventOutcome outcome, int page, int size) {
        var result = loginEventJpaRepository.findHistory(userUuid, outcome,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))));
        return new PageResult<>(result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(),
                result.isFirst(), result.isLast());
    }

    private LoginEvent toDomain(LoginEventJpaEntity entity) {
        return new LoginEvent(entity.getId(), entity.getUser() == null ? null : entity.getUser().getId(),
                entity.getUser() == null ? null : entity.getUser().getUuid(), entity.getOutcome(),
                entity.getOccurredAt(), entity.getIpAddress(), entity.getUserAgent(), entity.getFailureCategory());
    }
}
