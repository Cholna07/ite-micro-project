package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.result.LoginResult;
import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;
import kh.edu.istad.platform.iam.domain.usecase.user.LoginUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordLoginEventUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RegisterUserUseCase;
import kh.edu.istad.platform.iam.domain.dto.command.LoginSessionContext;
import kh.edu.istad.platform.restapi.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerLoginHistoryTest {
    @Mock RegisterUserUseCase registerUserUseCase;
    @Mock LoginUserUseCase loginUserUseCase;
    @Mock RecordLoginEventUseCase recordLoginEventUseCase;
    @InjectMocks AuthController controller;

    @Test
    void recordsExactlyOneSuccessfulEventAfterAuthentication() {
        when(loginUserUseCase.execute(any(), any(LoginSessionContext.class))).thenReturn(new LoginResult("jwt", "Bearer", UUID.randomUUID(), "user", List.of(), List.of()));
        MockHttpServletRequest request = request();

        controller.login(new LoginRequest("user", "secret"), request);

        verify(recordLoginEventUseCase, times(1)).execute(eq("user"), eq(LoginEventOutcome.SUCCESS),
                eq("192.0.2.5"), eq("test-agent"), isNull());
    }

    @Test
    void recordsFailureWithoutChangingTheAuthenticationFailure() {
        when(loginUserUseCase.execute(any(), any(LoginSessionContext.class))).thenThrow(new IllegalArgumentException("Invalid username/email or password"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.login(new LoginRequest("user", "wrong-secret"), request()));

        verify(recordLoginEventUseCase, times(1)).execute(eq("user"), eq(LoginEventOutcome.FAILURE),
                eq("192.0.2.5"), eq("test-agent"), eq("INVALID_CREDENTIALS"));
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.5");
        request.addHeader("User-Agent", "test-agent");
        return request;
    }
}
