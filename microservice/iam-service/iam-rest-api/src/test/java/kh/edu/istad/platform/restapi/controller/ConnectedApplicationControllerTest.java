package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.usecase.user.GetCurrentUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ConnectedApplicationControllerTest {
    private JdbcTemplate jdbcTemplate;
    private GetCurrentUserUseCase currentUser;
    private ConnectedApplicationController controller;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        currentUser = mock(GetCurrentUserUseCase.class);
        when(currentUser.execute()).thenReturn(new UserResult(UUID.randomUUID(), "alice", "alice@example.test",
                null, null, null, null, null, null, false, false, true, false, null, null));
        controller = new ConnectedApplicationController(jdbcTemplate, currentUser);
    }

    @Test
    void listsOnlyConsentsForCurrentAccount() {
        when(jdbcTemplate.query(any(String.class), any(RowMapper.class), eq("alice"))).thenReturn(List.of());

        assertEquals(List.of(), controller.list());

        verify(jdbcTemplate).query(any(String.class), any(RowMapper.class), eq("alice"));
        verify(currentUser).execute();
    }

    @Test
    void revokesOnlyCurrentAccountsAuthorization() {
        when(jdbcTemplate.update(any(String.class), eq("registered-client-id"), eq("alice"))).thenReturn(1);

        controller.revoke("registered-client-id");

        verify(jdbcTemplate, times(2)).update(any(String.class), eq("registered-client-id"), eq("alice"));
        verify(currentUser).execute();
    }

    @Test
    void doesNotDeleteAuthorizationsWhenCurrentAccountHasNoConsent() {
        when(jdbcTemplate.update(any(String.class), eq("another-client"), eq("alice"))).thenReturn(0);

        assertThrows(ResponseStatusException.class, () -> controller.revoke("another-client"));

        verify(jdbcTemplate).update(any(String.class), eq("another-client"), eq("alice"));
        verifyNoMoreInteractions(jdbcTemplate);
    }
}
