package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEvent;
import kh.edu.istad.platform.iam.domain.usecase.user.ListAccountActivityUseCase;
import kh.edu.istad.platform.restapi.dto.response.AdminAccountActivityResponse;
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

@RestController
@RequestMapping("/api/admin/account-activities")
@RequiredArgsConstructor
public class AdminAccountActivityController {
    private final ListAccountActivityUseCase listAccountActivityUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<AdminAccountActivityResponse>> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        PageResult<AccountActivityEvent> result;
        try {
            result = listAccountActivityUseCase.executeAll(page, size);
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        List<AdminAccountActivityResponse> content = result.content().stream()
                .map(event -> new AdminAccountActivityResponse(event.id(), event.userUuid(), event.username(),
                        event.eventType(), event.description(), event.occurredAt(), event.ipAddress(), event.userAgent()))
                .toList();
        return ResponseEntity.ok(new PageResponse<>(content, new PageMetadata(result.page(), result.size(),
                result.totalElements(), result.totalPages(), result.first(), result.last(), content.isEmpty())));
    }
}
