package kh.edu.istad.platform.iam.domain.dto.query;

public record UserFilterQuery(
        String keyword,
        Boolean enabled,
        Boolean emailVerified,
        Boolean deleted,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
}
