package kh.edu.istad.platform.iam.domain.port.out;

import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.dto.query.RoleFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {

    // Role retrieval
    Optional<Role> findByUuid(UUID uuid);

    Optional<Role> findByName(String name);

    List<Role> findAll();
    PageResult<Role> findPage(RoleFilterQuery query);

    // Role creation and update
    Role save(Role role);

    // Role deletion
    void delete(Role role);

    // Duplicate checking
    boolean existsByName(String name);

}
