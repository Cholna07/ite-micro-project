package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.domain.dto.command.CreatePermissionCommand;
import kh.edu.istad.platform.iam.domain.dto.command.UpdatePermissionCommand;
import kh.edu.istad.platform.iam.domain.dto.query.PermissionFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.dto.result.PermissionResult;
import kh.edu.istad.platform.iam.domain.usecase.permission.ActivatePermissionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.permission.CreatePermissionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.permission.DeactivatePermissionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.permission.DeletePermissionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.permission.GetPermissionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.permission.ListPermissionsUseCase;
import kh.edu.istad.platform.iam.domain.usecase.permission.UpdatePermissionUseCase;
import kh.edu.istad.platform.iam.domain.exception.PermissionNameConflictException;
import kh.edu.istad.platform.restapi.dto.request.CreatePermissionRequest;
import kh.edu.istad.platform.restapi.dto.request.UpdatePermissionRequest;
import kh.edu.istad.platform.restapi.dto.response.PageMetadata;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import kh.edu.istad.platform.restapi.dto.response.PermissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import kh.edu.istad.platform.restapi.exception.RestApiException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final CreatePermissionUseCase createPermissionUseCase;
    private final ListPermissionsUseCase listPermissionsUseCase;
    private final GetPermissionUseCase getPermissionUseCase;
    private final UpdatePermissionUseCase updatePermissionUseCase;
    private final DeletePermissionUseCase deletePermissionUseCase;
    private final ActivatePermissionUseCase activatePermissionUseCase;
    private final DeactivatePermissionUseCase deactivatePermissionUseCase;

    // 1. Create permission
    @PostMapping
    public ResponseEntity<PermissionResponse> createPermission(
            @Valid @RequestBody CreatePermissionRequest request
    ) {
        CreatePermissionCommand command = new CreatePermissionCommand(
                request.name(),
                request.resource(),
                request.action(),
                request.description()
        );

        try {
            PermissionResult result = createPermissionUseCase.execute(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
        } catch (PermissionNameConflictException | DataIntegrityViolationException exception) {
            throw new RestApiException(HttpStatus.CONFLICT, "Permission name already exists");
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    // 2. List permissions
    @GetMapping
    public ResponseEntity<PageResponse<PermissionResponse>> listPermissions(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "resource", required = false) String resource,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean systemPermission,
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "ASC") String sortDirection
    ) {
        PermissionFilterQuery query = new PermissionFilterQuery(
                keyword,
                resource,
                action,
                active,
                systemPermission,
                deleted,
                page,
                size,
                sortBy,
                sortDirection
        );

        PageResult<PermissionResult> result;
        try {
            result = listPermissionsUseCase.execute(query);
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        List<PermissionResponse> content = result.content()
                .stream()
                .map(this::toResponse)
                .toList();

        PageMetadata metadata = new PageMetadata(
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.first(),
                result.last(),
                content.isEmpty()
        );

        return ResponseEntity.ok(new PageResponse<>(content, metadata));
    }

    // 3. Get permission by UUID
    @GetMapping("/{uuid}")
    public ResponseEntity<PermissionResponse> getPermission(
            @PathVariable UUID uuid
    ) {
        PermissionResult result;
        try {
            result = getPermissionUseCase.execute(uuid);
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.NOT_FOUND, "Permission not found");
        } catch (IllegalStateException exception) {
            throw new RestApiException(HttpStatus.NOT_FOUND, "Permission not found");
        }

        return ResponseEntity.ok(toResponse(result));
    }

    // 4. Update permission
    @PutMapping("/{uuid}")
    public ResponseEntity<PermissionResponse> updatePermission(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        UpdatePermissionCommand command = new UpdatePermissionCommand(
                uuid,
                request.name(),
                request.resource(),
                request.action(),
                request.description()
        );

        try {
            PermissionResult result = updatePermissionUseCase.execute(command);
            return ResponseEntity.ok(toResponse(result));
        } catch (PermissionNameConflictException | DataIntegrityViolationException exception) {
            throw new RestApiException(HttpStatus.CONFLICT, "Permission name already exists");
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (IllegalStateException exception) {
            throw new RestApiException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    // 5. Delete permission
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deletePermission(
            @PathVariable UUID uuid
    ) {
        try {
            deletePermissionUseCase.execute(uuid);
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(HttpStatus.NOT_FOUND, "Permission not found");
        } catch (IllegalStateException exception) {
            throw new RestApiException(HttpStatus.CONFLICT, exception.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

    // 6. Activate permission
    @PutMapping("/{uuid}/activate")
    public ResponseEntity<Void> activatePermission(
            @PathVariable UUID uuid
    ) {
        try { activatePermissionUseCase.execute(uuid); }
        catch (IllegalArgumentException exception) { throw new RestApiException(HttpStatus.NOT_FOUND, "Permission not found"); }
        catch (IllegalStateException exception) { throw new RestApiException(HttpStatus.CONFLICT, exception.getMessage()); }

        return ResponseEntity.noContent().build();
    }

    // 7. Deactivate permission
    @PutMapping("/{uuid}/deactivate")
    public ResponseEntity<Void> deactivatePermission(
            @PathVariable UUID uuid
    ) {
        try { deactivatePermissionUseCase.execute(uuid); }
        catch (IllegalArgumentException exception) { throw new RestApiException(HttpStatus.NOT_FOUND, "Permission not found"); }
        catch (IllegalStateException exception) { throw new RestApiException(HttpStatus.CONFLICT, exception.getMessage()); }

        return ResponseEntity.noContent().build();
    }

    private PermissionResponse toResponse(PermissionResult result) {
        return new PermissionResponse(
                result.uuid(),
                result.name(),
                result.resource(),
                result.action(),
                result.description(),
                result.active(),
                result.system(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
