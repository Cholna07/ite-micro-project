package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.domain.dto.command.LoginUserCommand;
import kh.edu.istad.platform.iam.domain.dto.command.LoginSessionContext;
import kh.edu.istad.platform.iam.domain.dto.command.RegisterUserCommand;
import kh.edu.istad.platform.iam.domain.dto.result.LoginResult;
import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.usecase.user.LoginUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.AccountUnavailableException;
import kh.edu.istad.platform.iam.domain.usecase.user.DuplicateAccountException;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordLoginEventUseCase;
import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;
import kh.edu.istad.platform.iam.domain.usecase.user.RegisterUserUseCase;
import kh.edu.istad.platform.restapi.dto.request.LoginRequest;
import kh.edu.istad.platform.restapi.dto.request.RegisterUserRequest;
import kh.edu.istad.platform.restapi.dto.response.LoginResponse;
import kh.edu.istad.platform.restapi.dto.response.UserResponse;
import kh.edu.istad.platform.restapi.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RecordLoginEventUseCase recordLoginEventUseCase;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterUserRequest request
    ) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new RestApiException(
                    HttpStatus.BAD_REQUEST,
                    "Password and confirmation password do not match"
            );
        }

        RegisterUserCommand command = new RegisterUserCommand(
                request.username(),
                request.email(),
                request.password(),
                request.givenName(),
                request.familyName()
        );

        UserResult result;
        try {
            result = registerUserUseCase.execute(command);
        } catch (DuplicateAccountException duplicate) {
            throw new RestApiException(HttpStatus.CONFLICT, duplicate.getMessage());
        }

        UserResponse response = new UserResponse(
                result.uuid(),
                result.username(),
                result.email(),
                result.givenName(),
                result.familyName(),
                result.gender(),
                null,
                result.phoneNumber(),
                null,
                request.locale(),
                null,
                result.enabled(),
                result.accountLocked(),
                result.createdAt(),
                result.updatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        LoginUserCommand command = new LoginUserCommand(
                request.usernameOrEmail(),
                request.password()
        );

        LoginResult result;
        try {
            result = loginUserUseCase.execute(command,
                    new LoginSessionContext(httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent")));
        } catch (RuntimeException authenticationFailure) {
            recordLoginEventSafely(request.usernameOrEmail(), LoginEventOutcome.FAILURE,
                    httpRequest, failureCategory(authenticationFailure));
            if (authenticationFailure instanceof IllegalArgumentException
                    || authenticationFailure instanceof AccountUnavailableException) {
                throw new RestApiException(HttpStatus.UNAUTHORIZED,
                        "Invalid credentials or account unavailable");
            }
            throw authenticationFailure;
        }

        recordLoginEventSafely(request.usernameOrEmail(), LoginEventOutcome.SUCCESS, httpRequest, null);

        LoginResponse response = new LoginResponse(
                result.accessToken(),
                result.tokenType(),
                result.userUuid(),
                result.username(),
                result.roles(),
                result.permissions()
        );

        return ResponseEntity.ok(response);
    }

    private void recordLoginEventSafely(String usernameOrEmail, LoginEventOutcome outcome,
                                        HttpServletRequest request, String failureCategory) {
        try {
            recordLoginEventUseCase.execute(usernameOrEmail, outcome,
                    request.getRemoteAddr(), request.getHeader("User-Agent"), failureCategory);
        } catch (RuntimeException recordingFailure) {
            // Authentication outcome is decided only by LoginUserUseCase. A logging outage
            // must not change credential validation or turn a successful login into a failure.
            log.error("Could not persist login event with outcome {}", outcome, recordingFailure);
        }
    }

    private String failureCategory(RuntimeException exception) {
        if (exception instanceof IllegalArgumentException) return "INVALID_CREDENTIALS";
        if (exception instanceof AccountUnavailableException) return "ACCOUNT_UNAVAILABLE";
        return "AUTHENTICATION_FAILED";
    }
}
