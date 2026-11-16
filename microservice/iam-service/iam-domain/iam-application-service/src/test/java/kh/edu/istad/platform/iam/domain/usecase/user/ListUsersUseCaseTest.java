package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.query.UserFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListUsersUseCaseTest {
    @Test
    void returnsRepositoryPaginationMetadataAndPassesTheOriginalFilters() {
        UserRepository repository = mock(UserRepository.class);
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();
        when(user.getUuid()).thenReturn(userId);
        when(user.getUsername()).thenReturn(new Username("alice"));
        when(user.isEnabled()).thenReturn(true);
        when(user.isEmailVerified()).thenReturn(false);
        UserFilterQuery query = new UserFilterQuery("ali", true, false, false, 2, 5, "createdAt", "DESC");
        when(repository.findAll(query)).thenReturn(new PageResult<>(List.of(user), 2, 5, 11, 3, false, true));

        var page = new ListUsersUseCase(repository).execute(query);

        assertEquals(List.of(userId), page.content().stream().map(summary -> summary.uuid()).toList());
        assertEquals(2, page.page());
        assertEquals(5, page.size());
        assertEquals(11, page.totalElements());
        assertEquals(3, page.totalPages());
        assertFalse(page.first());
        assertTrue(page.last());
        verify(repository).findAll(query);
    }

    @Test
    void rejectsInvalidPageBoundsBeforeQueryingTheRepository() {
        UserRepository repository = mock(UserRepository.class);
        var useCase = new ListUsersUseCase(repository);
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(new UserFilterQuery(null, null, null, null, -1, 10, null, null)));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(new UserFilterQuery(null, null, null, null, 0, 101, null, null)));
        verifyNoInteractions(repository);
    }
}
