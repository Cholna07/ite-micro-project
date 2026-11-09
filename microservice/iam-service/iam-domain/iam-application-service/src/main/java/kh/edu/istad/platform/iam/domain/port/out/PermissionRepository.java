package kh.edu.istad.platform.iam.domain.port.out;

import kh.edu.istad.platform.iam.domain.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {

    // Permission retrieval
    Optional<Permission> findByUuid(UUID uuid);

    Optional<Permission> findByName(String name);

    List<Permission> findAll();

    // Permission creation and update
    Permission save(Permission permission);

    // Permission deletion
    void delete(Permission permission);

    // Duplicate checking
    boolean existsByName(String name);

}