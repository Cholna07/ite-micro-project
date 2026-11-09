package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.usecase.user.GetCurrentUserUseCase;
import kh.edu.istad.platform.restapi.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @GetMapping
    public ResponseEntity<UserResponse> getCurrentUser() {

        UserResult result = getCurrentUserUseCase.execute();

        UserResponse response = new UserResponse(
                result.uuid(),
                result.username(),
                result.email(),
                result.givenName(),
                result.familyName(),
                result.gender(),
                null, // birthdate is not included in UserResult
                result.phoneNumber(),
                null, // zoneInfo is not included in UserResult
                null, // locale is not included in UserResult
                null, // address is not included in UserResult
                result.enabled(),
                result.accountLocked(),
                result.createdAt(),
                result.updatedAt()
        );

        return ResponseEntity.ok(response);
    }
}
