package kh.edu.istad.platform.iam.domain.usecase.permission;

import kh.edu.istad.platform.iam.domain.dto.query.PermissionFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.dto.result.PermissionResult;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
@Component
public class ListPermissionsUseCase {

    private final PermissionRepository permissionRepository;

    public ListPermissionsUseCase(
            PermissionRepository permissionRepository
    ) {
        this.permissionRepository = permissionRepository;
    }

    public PageResult<PermissionResult> execute(PermissionFilterQuery query) {

        if (query == null) {
            throw new IllegalArgumentException(
                    "Permission filter query is required"
            );
        }

        if (query.page() < 0 || query.size() <= 0) {
            throw new IllegalArgumentException(
                    "Page must be zero or greater and size must be greater than zero"
            );
        }

        List<Permission> permissions = permissionRepository.findAll();

        // Apply keyword filter
        if (query.keyword() != null && !query.keyword().isBlank()) {
            String keyword = query.keyword()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            permissions = permissions.stream()
                    .filter(permission ->
                            permission.getName().value().toLowerCase(Locale.ROOT)
                                    .contains(keyword)
                                    || permission.getResource().value()
                                    .toLowerCase(Locale.ROOT).contains(keyword)
                                    || permission.getAction().value()
                                    .toLowerCase(Locale.ROOT).contains(keyword)
                                    || (permission.getDescription() != null
                                    && permission.getDescription()
                                    .toLowerCase(Locale.ROOT).contains(keyword))
                    )
                    .toList();
        }

        // Apply resource filter
        if (query.resource() != null && !query.resource().isBlank()) {
            String resource = query.resource()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            permissions = permissions.stream()
                    .filter(permission ->
                            permission.getResource().value()
                                    .equalsIgnoreCase(resource))
                    .toList();
        }

        // Apply action filter
        if (query.action() != null && !query.action().isBlank()) {
            String action = query.action()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            permissions = permissions.stream()
                    .filter(permission ->
                            permission.getAction().value()
                                    .equalsIgnoreCase(action))
                    .toList();
        }

        // Apply active filter
        if (query.active() != null) {
            permissions = permissions.stream()
                    .filter(permission ->
                            permission.isActive() == query.active())
                    .toList();
        }

        // Apply system permission filter
        if (query.systemPermission() != null) {
            permissions = permissions.stream()
                    .filter(permission ->
                            permission.isSystem() == query.systemPermission())
                    .toList();
        }

        // Apply deleted filter
        if (query.deleted() == null || !query.deleted()) {
            permissions = permissions.stream()
                    .filter(permission -> !permission.isDeleted())
                    .toList();
        } else {
            permissions = permissions.stream()
                    .filter(Permission::isDeleted)
                    .toList();
        }

        // Apply sorting
        Comparator<Permission> comparator = Comparator.comparing(
                permission -> permission.getName().value(),
                String.CASE_INSENSITIVE_ORDER
        );

        if ("resource".equalsIgnoreCase(query.sortBy())) {
            comparator = Comparator.comparing(
                    permission -> permission.getResource().value(),
                    String.CASE_INSENSITIVE_ORDER
            );
        } else if ("action".equalsIgnoreCase(query.sortBy())) {
            comparator = Comparator.comparing(
                    permission -> permission.getAction().value(),
                    String.CASE_INSENSITIVE_ORDER
            );
        } else if ("createdAt".equalsIgnoreCase(query.sortBy())) {
            comparator = Comparator.comparing(
                    Permission::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
        }

        if ("desc".equalsIgnoreCase(query.sortDirection())) {
            comparator = comparator.reversed();
        }

        permissions = permissions.stream()
                .sorted(comparator)
                .toList();

        // Calculate pagination
        int totalElements = permissions.size();
        int totalPages = (int) Math.ceil(
                (double) totalElements / query.size()
        );

        int start = Math.min(
                query.page() * query.size(),
                totalElements
        );

        int end = Math.min(
                start + query.size(),
                totalElements
        );

        List<PermissionResult> content = permissions
                .subList(start, end)
                .stream()
                .map(this::toResult)
                .toList();

        return new PageResult<>(
                content,
                query.page(),
                query.size(),
                totalElements,
                totalPages,
                query.page() == 0,
                query.page() >= totalPages - 1
        );
    }

    private PermissionResult toResult(Permission permission) {
        return new PermissionResult(
                permission.getUuid(),
                permission.getName().value(),
                permission.getResource().value(),
                permission.getAction().value(),
                permission.getDescription(),
                permission.isSystem(),
                permission.isActive(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}
