package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEvent;
import kh.edu.istad.platform.iam.domain.port.out.AccountActivityRepository;
import kh.edu.istad.platform.iam.persistence.entity.AccountActivityEventJpaEntity;
import kh.edu.istad.platform.iam.persistence.repository.AccountActivityEventJpaRepository;
import kh.edu.istad.platform.iam.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
public class AccountActivityRepositoryAdapter implements AccountActivityRepository {
    private final AccountActivityEventJpaRepository activityRepository;
    private final UserJpaRepository userRepository;

    @Override
    public AccountActivityEvent save(AccountActivityEvent event) {
        AccountActivityEventJpaEntity entity = new AccountActivityEventJpaEntity();
        entity.setId(event.id());
        entity.setUser(userRepository.findByUuid(event.userUuid())
                .orElseThrow(() -> new IllegalArgumentException("Account activity user does not exist")));
        entity.setEventType(event.eventType());
        entity.setDescription(event.description());
        entity.setOccurredAt(event.occurredAt());
        entity.setIpAddress(event.ipAddress());
        entity.setUserAgent(event.userAgent());
        return toDomain(activityRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<AccountActivityEvent> findByUserUuid(java.util.UUID userUuid, int page, int size) {
        var result = activityRepository.findByUserUuid(userUuid, PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id"))));
        return new PageResult<>(result.getContent().stream().map(this::toDomain).toList(), result.getNumber(),
                result.getSize(), result.getTotalElements(), result.getTotalPages(), result.isFirst(), result.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<AccountActivityEvent> findAll(int page, int size) {
        var result = activityRepository.findAllActivities(PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id"))));
        return new PageResult<>(result.getContent().stream().map(this::toDomain).toList(), result.getNumber(),
                result.getSize(), result.getTotalElements(), result.getTotalPages(), result.isFirst(), result.isLast());
    }

    private AccountActivityEvent toDomain(AccountActivityEventJpaEntity entity) {
        return new AccountActivityEvent(entity.getId(), entity.getUser().getUuid(), entity.getEventType(),
                entity.getDescription(), entity.getOccurredAt(), entity.getIpAddress(), entity.getUserAgent(),
                entity.getUser().getUsername());
    }
}
