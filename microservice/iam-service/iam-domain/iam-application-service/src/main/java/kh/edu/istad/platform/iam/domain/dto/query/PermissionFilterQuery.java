package kh.edu.istad.platform.iam.domain.dto.query;

public record PermissionFilterQuery(
        String keyword,
        String resource,
        String action,
        Boolean active,
        Boolean systemPermission,
        Boolean deleted,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
}
