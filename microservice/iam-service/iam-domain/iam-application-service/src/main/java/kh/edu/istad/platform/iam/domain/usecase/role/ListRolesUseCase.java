package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.dto.query.RoleFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.dto.result.RoleSummary;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
@Component
public class ListRolesUseCase {

    private final RoleRepository roleRepository;

    public ListRolesUseCase(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public PageResult<RoleSummary> execute(RoleFilterQuery query) {

        if (query == null) {
            throw new IllegalArgumentException("Role filter query is required");
        }

        if (query.page() < 0 || query.size() < 1 || query.size() > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
        Set<String> sortFields = Set.of("name", "description", "createdAt", "updatedAt", "active", "systemRole");
        if (query.sortBy() == null || !sortFields.contains(query.sortBy())) {
            throw new IllegalArgumentException("Unsupported role sort field");
        }
        if (query.sortDirection() == null || !(query.sortDirection().equalsIgnoreCase("ASC")
                || query.sortDirection().equalsIgnoreCase("DESC"))) {
            throw new IllegalArgumentException("Sort direction must be ASC or DESC");
        }

        var page = roleRepository.findPage(query);
        var content = page.content()
                .stream()
                .map(role -> new RoleSummary(
                        role.getUuid(),
                        role.getName().value(),
                        role.getDescription(),
                        role.isSystem(),
                        role.isActive()
                ))
                .toList();

        return new PageResult<>(
                content,
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.first(),
                page.last()
        );
    }
}
