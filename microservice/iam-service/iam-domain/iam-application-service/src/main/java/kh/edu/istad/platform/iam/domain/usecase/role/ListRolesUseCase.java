package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.dto.query.RoleFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.dto.result.RoleSummary;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
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

        List<Role> filteredRoles = roleRepository.findAll()
                .stream()
                .filter(role -> !role.isDeleted() ||
                        Boolean.TRUE.equals(query.deleted()))
                .filter(role -> query.keyword() == null
                        || query.keyword().isBlank()
                        || role.getName().value().toLowerCase()
                        .contains(query.keyword().trim().toLowerCase())
                        || (role.getDescription() != null
                        && role.getDescription().toLowerCase()
                        .contains(query.keyword().trim().toLowerCase())))
                .filter(role -> query.active() == null
                        || role.isActive() == query.active())
                .filter(role -> query.systemRole() == null
                        || role.isSystem() == query.systemRole())
                .toList();

        int page = Math.max(query.page(), 0);
        int size = query.size() <= 0 ? 10 : query.size();

        int fromIndex = Math.min(page * size, filteredRoles.size());
        int toIndex = Math.min(fromIndex + size, filteredRoles.size());

        List<RoleSummary> content = filteredRoles
                .subList(fromIndex, toIndex)
                .stream()
                .map(role -> new RoleSummary(
                        role.getUuid(),
                        role.getName().value(),
                        role.getDescription(),
                        role.isSystem(),
                        role.isActive()
                ))
                .toList();

        long totalElements = filteredRoles.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new PageResult<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                page >= totalPages - 1
        );
    }
}
