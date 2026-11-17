package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.query.AuditLogFilter;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.AuditLogEvent;
import kh.edu.istad.platform.iam.domain.entity.AuditLogOutcome;
import kh.edu.istad.platform.iam.domain.usecase.audit.ListAuditLogsUseCase;
import kh.edu.istad.platform.restapi.dto.response.AuditLogResponse;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {
    private final ListAuditLogsUseCase listAuditLogsUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<AuditLogResponse>> list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "actorUuid", required = false) UUID actorUuid,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "targetType", required = false) String targetType,
            @RequestParam(name = "targetId", required = false) String targetId,
            @RequestParam(name = "outcome", required = false) AuditLogOutcome outcome,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "25") int size,
            @RequestParam(name = "sortBy", defaultValue = "occurredAt") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "DESC") String sortDirection) {
        PageResult<AuditLogEvent> result;
        try {
            result = listAuditLogsUseCase.execute(new AuditLogFilter(keyword, actorUuid, action,
                    targetType, targetId, outcome, from, to, page, size, sortBy, sortDirection));
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        List<AuditLogResponse> content = result.content().stream().map(AuditLogResponse::from).toList();
        return ResponseEntity.ok(new PageResponse<>(content, new PageMetadata(result.page(), result.size(),
                result.totalElements(), result.totalPages(), result.first(), result.last(), content.isEmpty())));
    }
}
