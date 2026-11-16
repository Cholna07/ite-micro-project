package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;
import kh.edu.istad.platform.iam.domain.port.out.LoginEventRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.ChangePasswordUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.DisableUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.GetCurrentUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListLoginHistoryUseCase;
import kh.edu.istad.platform.iam.domain.usecase.identity.ListUserIdentitiesUseCase;
import kh.edu.istad.platform.iam.domain.usecase.identity.UnlinkIdentityUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.UpdateUserProfileUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginHistoryScopeAndPaginationTest {
    @Mock GetCurrentUserUseCase getCurrentUserUseCase;
    @Mock ListLoginHistoryUseCase listLoginHistoryUseCase;
    @Mock LoginEventRepository loginEventRepository;

    @Test
    void accountHistoryAlwaysUsesAuthenticatedUsersUuidAndHasNoTargetUserInput() {
        UUID authenticatedUser = UUID.randomUUID();
        when(getCurrentUserUseCase.execute()).thenReturn(new UserResult(authenticatedUser, "alice", "a@example.test",
                null, null, null, null, null, null, false, false, true, false, Instant.now(), null));
        when(listLoginHistoryUseCase.execute(authenticatedUser, LoginEventOutcome.FAILURE, 2, 10))
                .thenReturn(new PageResult<>(List.of(), 2, 10, 0, 0, false, true));
        MeController controller = new MeController(getCurrentUserUseCase, mock(ListUserIdentitiesUseCase.class),
                mock(UnlinkIdentityUseCase.class), mock(DisableUserUseCase.class), mock(UpdateUserProfileUseCase.class),
                mock(ChangePasswordUseCase.class), listLoginHistoryUseCase, mock(kh.edu.istad.platform.iam.domain.usecase.user.ListAccountActivityUseCase.class));

        controller.getCurrentUserLoginHistory(LoginEventOutcome.FAILURE, 2, 10);

        verify(listLoginHistoryUseCase).execute(authenticatedUser, LoginEventOutcome.FAILURE, 2, 10);
        verifyNoMoreInteractions(listLoginHistoryUseCase);
    }

    @Test
    void listUseCaseForwardsAdminFiltersAndPaginationToRepository() {
        UUID targetUser = UUID.randomUUID();
        PageResult<kh.edu.istad.platform.iam.domain.entity.LoginEvent> expected =
                new PageResult<>(List.of(), 3, 15, 0, 0, false, true);
        when(loginEventRepository.findHistory(targetUser, LoginEventOutcome.SUCCESS, 3, 15)).thenReturn(expected);

        PageResult<?> actual = new ListLoginHistoryUseCase(loginEventRepository)
                .execute(targetUser, LoginEventOutcome.SUCCESS, 3, 15);

        assertSame(expected, actual);
        verify(loginEventRepository).findHistory(targetUser, LoginEventOutcome.SUCCESS, 3, 15);
    }

    @Test
    void rejectsUnboundedOrInvalidPagination() {
        ListLoginHistoryUseCase useCase = new ListLoginHistoryUseCase(loginEventRepository);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, null, -1, 20));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, null, 0, 101));
        verifyNoInteractions(loginEventRepository);
    }
}
