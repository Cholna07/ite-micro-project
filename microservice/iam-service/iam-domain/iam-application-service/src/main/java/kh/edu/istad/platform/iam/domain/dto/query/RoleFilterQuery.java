package kh.edu.istad.platform.iam.domain.dto.query;

public record RoleFilterQuery(
        String keyword,
        Boolean active,
        Boolean systemRole,
        Boolean deleted,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
}
