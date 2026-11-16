package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.SessionCredentialType;
import kh.edu.istad.platform.iam.domain.entity.UserSession;
import kh.edu.istad.platform.iam.persistence.entity.UserJpaEntity;
import kh.edu.istad.platform.iam.persistence.entity.UserSessionJpaEntity;
import kh.edu.istad.platform.iam.persistence.repository.UserJpaRepository;
import kh.edu.istad.platform.iam.persistence.repository.UserSessionJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionRepositoryAdapterTest {
    @Mock UserSessionJpaRepository sessionRepository;
    @Mock UserJpaRepository userRepository;

    @Test
    void savesSessionMetadataWithoutAnyCredentialMaterial() {
        UUID userUuid = UUID.randomUUID();
        UserJpaEntity user = user(userUuid);
        when(sessionRepository.findById(any())).thenReturn(Optional.empty());
        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UserSessionRepositoryAdapter adapter = new UserSessionRepositoryAdapter(sessionRepository, userRepository);
        UserSession session = session(userUuid);

        UserSession result = adapter.save(session);

        assertEquals(session, result);
        ArgumentCaptor<UserSessionJpaEntity> captor = ArgumentCaptor.forClass(UserSessionJpaEntity.class);
        verify(sessionRepository).save(captor.capture());
        assertEquals(session.id(), captor.getValue().getId());
        assertEquals(user, captor.getValue().getUser());
        assertEquals(SessionCredentialType.JWT, captor.getValue().getCredentialType());
        assertNull(captor.getValue().getRevokedAt());
    }

    @Test
    void listsOnlyActiveRowsInRepositoryPageOrder() {
        UUID userUuid = UUID.randomUUID();
        UserJpaEntity user = user(userUuid);
        UserSessionJpaEntity entity = entity(session(userUuid), user);
        when(sessionRepository.findActiveByUser(eq(userUuid), any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));
        UserSessionRepositoryAdapter adapter = new UserSessionRepositoryAdapter(sessionRepository, userRepository);

        PageResult<UserSession> page = adapter.findActiveByUser(userUuid, Instant.now(), 0, 10);

        assertEquals(1, page.totalElements());
        assertEquals(entity.getId(), page.content().getFirst().id());
        assertEquals(userUuid, page.content().getFirst().userUuid());
    }

    @Test
    void checksSessionBelongsToTheRequestedUserAndIsActive() {
        UserSessionRepositoryAdapter adapter = new UserSessionRepositoryAdapter(sessionRepository, userRepository);
        UUID id = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        when(sessionRepository.existsActive(eq(id), eq(userUuid), any())).thenReturn(true);

        assertTrue(adapter.isActive(id, userUuid, Instant.now()));
    }

    @Test
    void touchesJwtActivityOnlyForTheOwningUser() {
        UserSessionRepositoryAdapter adapter = new UserSessionRepositoryAdapter(sessionRepository, userRepository);
        UUID id = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        when(sessionRepository.touchActivity(eq(id), eq(userUuid), any())).thenReturn(1);

        assertTrue(adapter.touchActivity(id, userUuid, Instant.now()));
        verify(sessionRepository).touchActivity(eq(id), eq(userUuid), any());
    }

    @Test
    void doesNotRevokeASessionOwnedByAnotherUser() {
        UUID ownerUuid = UUID.randomUUID();
        UUID requesterUuid = UUID.randomUUID();
        UserJpaEntity owner = user(ownerUuid);
        UserSession session = session(ownerUuid);
        UserSessionJpaEntity entity = entity(session, owner);
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(entity));
        UserSessionRepositoryAdapter adapter = new UserSessionRepositoryAdapter(sessionRepository, userRepository);

        assertFalse(adapter.revoke(session.id(), requesterUuid, requesterUuid, Instant.now(), "REVOKED_BY_USER"));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void revokeOthersMarksOtherActiveRecordsAndPreservesCurrentRecord() {
        UUID ownerUuid = UUID.randomUUID();
        UUID currentId = UUID.randomUUID();
        UserJpaEntity owner = user(ownerUuid);
        UserSession current = session(currentId, ownerUuid);
        UserSession other = session(UUID.randomUUID(), ownerUuid);
        UserSessionJpaEntity currentEntity = entity(current, owner);
        UserSessionJpaEntity otherEntity = entity(other, owner);
        when(sessionRepository.findActiveForRevocation(eq(ownerUuid), any()))
                .thenReturn(List.of(currentEntity, otherEntity));
        when(userRepository.findByUuid(ownerUuid)).thenReturn(Optional.of(owner));
        when(sessionRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        UserSessionRepositoryAdapter adapter = new UserSessionRepositoryAdapter(sessionRepository, userRepository);

        assertEquals(1, adapter.revokeOthers(ownerUuid, currentId, ownerUuid, Instant.now(), "REVOKED_OTHER_SESSIONS"));

        assertNull(currentEntity.getRevokedAt());
        assertNotNull(otherEntity.getRevokedAt());
    }

    private UserSession session(UUID userUuid) {
        return session(UUID.randomUUID(), userUuid);
    }

    private UserSession session(UUID id, UUID userUuid) {
        Instant created = Instant.parse("2026-11-17T00:00:00Z");
        return new UserSession(id, userUuid, SessionCredentialType.JWT, "PASSWORD", null,
                created, created, created.plusSeconds(900), "127.0.0.1", "test-browser", null, null, null);
    }

    private UserSessionJpaEntity entity(UserSession session, UserJpaEntity user) {
        UserSessionJpaEntity entity = new UserSessionJpaEntity();
        entity.setId(session.id());
        entity.setUser(user);
        entity.setCredentialType(session.credentialType());
        entity.setAuthenticationType(session.authenticationType());
        entity.setProvider(session.provider());
        entity.setCreatedAt(session.createdAt());
        entity.setLastActivityAt(session.lastActivityAt());
        entity.setExpiresAt(session.expiresAt());
        entity.setIpAddress(session.ipAddress());
        entity.setUserAgent(session.userAgent());
        return entity;
    }

    private UserJpaEntity user(UUID uuid) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setUuid(uuid);
        return entity;
    }
}
