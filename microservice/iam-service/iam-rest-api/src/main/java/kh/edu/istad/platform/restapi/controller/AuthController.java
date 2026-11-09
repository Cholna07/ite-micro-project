package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.domain.dto.command.LoginUserCommand;
import kh.edu.istad.platform.iam.domain.dto.command.RegisterUserCommand;
import kh.edu.istad.platform.iam.domain.dto.result.LoginResult;
import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.usecase.user.LoginUserUseCase;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

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

        UserResult result = registerUserUseCase.execute(command);

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
            @Valid @RequestBody LoginRequest request
    ) {
        LoginUserCommand command = new LoginUserCommand(
                request.usernameOrEmail(),
                request.password()
        );

        LoginResult result = loginUserUseCase.execute(command);

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
}