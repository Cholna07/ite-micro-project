package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.LoginEvent;
import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;
import kh.edu.istad.platform.iam.domain.usecase.user.ListLoginHistoryUseCase;
import kh.edu.istad.platform.restapi.dto.response.LoginEventResponse;
import kh.edu.istad.platform.restapi.dto.response.PageMetadata;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import kh.edu.istad.platform.restapi.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/login-history")
@RequiredArgsConstructor
public class AdminLoginHistoryController {
    private final ListLoginHistoryUseCase listLoginHistoryUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<LoginEventResponse>> list(
            @RequestParam(name = "userUuid", required = false) UUID userUuid,
            @RequestParam(name = "outcome", required = false) LoginEventOutcome outcome,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        PageResult<LoginEvent> result;
        try {
            result = listLoginHistoryUseCase.execute(userUuid, outcome, page, size);
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        List<LoginEventResponse> content = result.content().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(new PageResponse<>(content, new PageMetadata(
                result.page(), result.size(), result.totalElements(), result.totalPages(),
                result.first(), result.last(), content.isEmpty())));
    }

    private LoginEventResponse toResponse(LoginEvent event) {
        return new LoginEventResponse(event.id(), event.userUuid(), event.outcome(), event.occurredAt(),
                event.ipAddress(), event.userAgent(), event.failureCategory());
    }
}
