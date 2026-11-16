package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.port.out.AccountActivityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListAccountActivityUseCaseTest {
    @Mock AccountActivityRepository repository;

    @Test
    void validatesPaginationAndQueriesOnlyTheRequestedLocalUser() {
        UUID userUuid = UUID.randomUUID();
        when(repository.findByUserUuid(userUuid, 1, 20))
                .thenReturn(new PageResult<>(List.of(), 1, 20, 0, 0, false, true));

        new ListAccountActivityUseCase(repository).execute(userUuid, 1, 20);

        verify(repository).findByUserUuid(userUuid, 1, 20);
        assertThrows(IllegalArgumentException.class, () -> new ListAccountActivityUseCase(repository).execute(userUuid, -1, 20));
        assertThrows(IllegalArgumentException.class, () -> new ListAccountActivityUseCase(repository).execute(userUuid, 0, 101));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void validatesPaginationAndQueriesAdministrativeActivityAcrossUsers() {
        when(repository.findAll(0, 10)).thenReturn(new PageResult<>(List.of(), 0, 10, 0, 0, true, true));
        var useCase = new ListAccountActivityUseCase(repository);

        useCase.executeAll(0, 10);

        verify(repository).findAll(0, 10);
        assertThrows(IllegalArgumentException.class, () -> useCase.executeAll(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> useCase.executeAll(0, 101));
        verifyNoMoreInteractions(repository);
    }
}
