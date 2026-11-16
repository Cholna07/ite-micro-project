package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.persistence.entity.AccountActivityEventJpaEntity;
import kh.edu.istad.platform.iam.persistence.entity.UserJpaEntity;
import kh.edu.istad.platform.iam.persistence.repository.AccountActivityEventJpaRepository;
import kh.edu.istad.platform.iam.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountActivityRepositoryAdapterTest {
    @Mock AccountActivityEventJpaRepository events;
    @Mock UserJpaRepository users;

    @Test
    void queriesActivityNewestFirstWithStableIdTiebreaker() {
        UUID userUuid = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UserJpaEntity user = new UserJpaEntity();
        user.setUuid(userUuid);
        AccountActivityEventJpaEntity event = new AccountActivityEventJpaEntity();
        event.setId(eventId);
        event.setUser(user);
        event.setEventType(kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType.PROFILE_UPDATED);
        event.setDescription("Profile information updated");
        event.setOccurredAt(Instant.now());
        when(events.findByUserUuid(eq(userUuid), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));

        var page = new AccountActivityRepositoryAdapter(events, users).findByUserUuid(userUuid, 0, 20);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(events).findByUserUuid(eq(userUuid), pageable.capture());
        assertEquals("occurredAt: DESC,id: DESC", pageable.getValue().getSort().toString());
        assertEquals(eventId, page.content().get(0).id());
        verifyNoInteractions(users);
    }

    @Test
    void listsAllUsersActivityNewestFirstUsingDatabasePagination() {
        UUID userUuid = UUID.randomUUID();
        UserJpaEntity user = new UserJpaEntity();
        user.setUuid(userUuid);
        AccountActivityEventJpaEntity event = new AccountActivityEventJpaEntity();
        event.setId(UUID.randomUUID());
        event.setUser(user);
        event.setEventType(kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType.PROFILE_UPDATED);
        event.setDescription("Profile updated");
        event.setOccurredAt(Instant.now());
        when(events.findAllActivities(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event), org.springframework.data.domain.PageRequest.of(0, 10), 17));

        var page = new AccountActivityRepositoryAdapter(events, users).findAll(0, 10);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(events).findAllActivities(pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(10, pageable.getValue().getPageSize());
        assertEquals("occurredAt: DESC,id: DESC", pageable.getValue().getSort().toString());
        assertEquals(17, page.totalElements());
        assertEquals(userUuid, page.content().get(0).userUuid());
    }
}
