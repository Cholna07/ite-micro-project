package kh.edu.istad.platform.iam.domain.usecase.permission;

import kh.edu.istad.platform.iam.domain.dto.query.PermissionFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.dto.result.PermissionResult;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
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

        if (query.page() < 0 || query.size() < 1 || query.size() > 100)
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        Set<String> sortFields = Set.of("name", "resource", "action", "createdAt", "updatedAt", "active", "systemPermission");
        if (query.sortBy() == null || !sortFields.contains(query.sortBy()))
            throw new IllegalArgumentException("Unsupported permission sort field");
        if (query.sortDirection() == null || !(query.sortDirection().equalsIgnoreCase("ASC") || query.sortDirection().equalsIgnoreCase("DESC")))
            throw new IllegalArgumentException("Sort direction must be ASC or DESC");

        PageResult<Permission> page = permissionRepository.findPage(query);
        List<PermissionResult> content = page.content().stream().map(this::toResult).toList();
        return new PageResult<>(content, page.page(), page.size(), page.totalElements(), page.totalPages(), page.first(), page.last());
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
