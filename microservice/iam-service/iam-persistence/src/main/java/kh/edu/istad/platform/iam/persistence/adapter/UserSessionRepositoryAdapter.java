package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.UserSession;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.persistence.entity.UserJpaEntity;
import kh.edu.istad.platform.iam.persistence.entity.UserSessionJpaEntity;
import kh.edu.istad.platform.iam.persistence.repository.UserJpaRepository;
import kh.edu.istad.platform.iam.persistence.repository.UserSessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserSessionRepositoryAdapter implements UserSessionRepository {
    private final UserSessionJpaRepository sessionRepository;
    private final UserJpaRepository userRepository;

    @Override
    public UserSession save(UserSession session) {
        UserSessionJpaEntity entity = sessionRepository.findById(session.id()).orElseGet(UserSessionJpaEntity::new);
        entity.setId(session.id());
        entity.setUser(userRepository.findByUuid(session.userUuid())
                .orElseThrow(() -> new IllegalArgumentException("Session user not found")));
        entity.setCredentialType(session.credentialType());
        entity.setAuthenticationType(session.authenticationType());
        entity.setProvider(session.provider());
        entity.setCreatedAt(session.createdAt());
        entity.setLastActivityAt(session.lastActivityAt());
        entity.setExpiresAt(session.expiresAt());
        entity.setIpAddress(session.ipAddress());
        entity.setUserAgent(session.userAgent());
        entity.setRevokedAt(session.revokedAt());
        entity.setRevocationReason(session.revocationReason());
        entity.setRevokedByUser(resolveOptionalUser(session.revokedByUserUuid()));
        return toDomain(sessionRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSession> findById(UUID sessionId) {
        return sessionRepository.findById(sessionId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActive(UUID sessionId, UUID userUuid, Instant now) {
        return sessionRepository.existsActive(sessionId, userUuid, now);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserSession> findActiveByUser(UUID userUuid, Instant now, int page, int size) {
        var result = sessionRepository.findActiveByUser(userUuid, now,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastActivityAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))));
        return new PageResult<>(result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(),
                result.isFirst(), result.isLast());
    }

    @Override
    public boolean revoke(UUID sessionId, UUID userUuid, UUID revokedByUserUuid, Instant revokedAt, String reason) {
        Optional<UserSessionJpaEntity> optional = sessionRepository.findById(sessionId);
        if (optional.isEmpty()) return false;
        UserSessionJpaEntity entity = optional.get();
        if (!entity.getUser().getUuid().equals(userUuid)
                || entity.getRevokedAt() != null
                || (entity.getExpiresAt() != null && !entity.getExpiresAt().isAfter(revokedAt))) return false;
        entity.setRevokedAt(revokedAt);
        entity.setRevocationReason(reason);
        entity.setRevokedByUser(resolveOptionalUser(revokedByUserUuid));
        sessionRepository.save(entity);
        return true;
    }

    @Override
    public int revokeOthers(UUID userUuid, UUID currentSessionId, UUID revokedByUserUuid, Instant revokedAt, String reason) {
        return revokeMatching(userUuid, currentSessionId, revokedByUserUuid, revokedAt, reason, false);
    }

    @Override
    public int revokeAll(UUID userUuid, UUID revokedByUserUuid, Instant revokedAt, String reason) {
        return revokeMatching(userUuid, null, revokedByUserUuid, revokedAt, reason, true);
    }

    private int revokeMatching(UUID userUuid, UUID exceptSessionId, UUID revokedByUserUuid,
                               Instant revokedAt, String reason, boolean includeExcept) {
        UserJpaEntity actor = resolveOptionalUser(revokedByUserUuid);
        var sessions = sessionRepository.findActiveForRevocation(userUuid, revokedAt).stream()
                .filter(session -> includeExcept || !session.getId().equals(exceptSessionId))
                .toList();
        sessions.forEach(session -> {
            session.setRevokedAt(revokedAt);
            session.setRevokedByUser(actor);
            session.setRevocationReason(reason);
        });
        sessionRepository.saveAll(sessions);
        return sessions.size();
    }

    @Override
    public boolean updateActivity(UUID sessionId, Instant lastActivityAt, Instant expiresAt) {
        return sessionRepository.updateActivity(sessionId, lastActivityAt, expiresAt) == 1;
    }

    @Override
    public boolean touchActivity(UUID sessionId, UUID userUuid, Instant lastActivityAt) {
        return sessionRepository.touchActivity(sessionId, userUuid, lastActivityAt) == 1;
    }

    private UserJpaEntity resolveOptionalUser(UUID userUuid) {
        if (userUuid == null) return null;
        return userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("Session actor not found"));
    }

    private UserSession toDomain(UserSessionJpaEntity entity) {
        return new UserSession(entity.getId(), entity.getUser().getUuid(), entity.getCredentialType(),
                entity.getAuthenticationType(), entity.getProvider(), entity.getCreatedAt(),
                entity.getLastActivityAt(), entity.getExpiresAt(), entity.getIpAddress(), entity.getUserAgent(),
                entity.getRevokedAt(), entity.getRevokedByUser() == null ? null : entity.getRevokedByUser().getUuid(),
                entity.getRevocationReason());
    }
}
