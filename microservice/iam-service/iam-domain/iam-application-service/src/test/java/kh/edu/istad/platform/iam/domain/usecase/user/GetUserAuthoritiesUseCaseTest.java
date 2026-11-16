package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.RolePermission;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserPermission;
import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RolePermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserPermissionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRoleRepository;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionEffect;
import kh.edu.istad.platform.iam.domain.valueobject.PermissionName;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GetUserAuthoritiesUseCaseTest {
    @Test
    void inactiveAndDeletedPermissionsAreExcludedFromEffectiveAuthorities() {
        UUID userUuid = UUID.randomUUID(); User user = new User(); user.setId(1L); user.setUuid(userUuid);
        Role role = new Role(); role.setId(2L); role.setName(new RoleName("reader")); role.setActive(true);
        UserRole userRole = new UserRole(); userRole.setRoleId(2L);
        RolePermission inactiveAssignment = new RolePermission(); inactiveAssignment.setPermissionId(3L);
        RolePermission deletedAssignment = new RolePermission(); deletedAssignment.setPermissionId(4L);
        Permission inactive = permission(3L, "documents.read", false, false);
        Permission deleted = permission(4L, "documents.write", true, true); deleted.setDeleted(true);

        UserRepository users = mock(UserRepository.class); RoleRepository roles = mock(RoleRepository.class);
        PermissionRepository permissions = mock(PermissionRepository.class); UserRoleRepository userRoles = mock(UserRoleRepository.class);
        RolePermissionRepository rolePermissions = mock(RolePermissionRepository.class); UserPermissionRepository userPermissions = mock(UserPermissionRepository.class);
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user)); when(userRoles.findActiveAssignmentsByUserId(1L)).thenReturn(List.of(userRole));
        when(roles.findAll()).thenReturn(List.of(role)); when(permissions.findAll()).thenReturn(List.of(inactive, deleted));
        when(rolePermissions.findActiveAssignmentsByRoleId(2L)).thenReturn(List.of(inactiveAssignment, deletedAssignment));
        when(userPermissions.findActiveAssignmentsByUserId(1L)).thenReturn(List.of());

        var result = new GetUserAuthoritiesUseCase(users, roles, permissions, userRoles, rolePermissions, userPermissions).execute(userUuid);
        assertEquals(List.of(), result.permissions());
    }

    @Test
    void directDenyOverridesInheritedAndDirectGrantsInEffectiveResult() {
        UUID userUuid = UUID.randomUUID();
        User user = new User();
        user.setId(1L);
        user.setUuid(userUuid);
        Role role = new Role();
        role.setId(2L);
        role.setName(new RoleName("reader"));
        role.setActive(true);
        Permission permission = new Permission();
        permission.setId(3L);
        permission.setName(new PermissionName("documents.read"));
        permission.setActive(true);
        UserRole userRole = new UserRole();
        userRole.setRoleId(2L);
        RolePermission inheritedGrant = new RolePermission();
        inheritedGrant.setPermissionId(3L);
        UserPermission directGrant = directAssignment(PermissionEffect.GRANT);
        UserPermission directDeny = directAssignment(PermissionEffect.DENY);

        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        PermissionRepository permissions = mock(PermissionRepository.class);
        UserRoleRepository userRoles = mock(UserRoleRepository.class);
        RolePermissionRepository rolePermissions = mock(RolePermissionRepository.class);
        UserPermissionRepository userPermissions = mock(UserPermissionRepository.class);
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(userRoles.findActiveAssignmentsByUserId(1L)).thenReturn(List.of(userRole));
        when(roles.findAll()).thenReturn(List.of(role));
        when(permissions.findAll()).thenReturn(List.of(permission));
        when(rolePermissions.findActiveAssignmentsByRoleId(2L)).thenReturn(List.of(inheritedGrant));
        // A conflicting pair can exist in legacy rows or through a historical concurrency race;
        // the effective calculator explicitly applies DENY precedence to both sources.
        when(userPermissions.findActiveAssignmentsByUserId(1L)).thenReturn(List.of(directGrant, directDeny));

        var result = new GetUserAuthoritiesUseCase(users, roles, permissions, userRoles,
                rolePermissions, userPermissions).execute(userUuid);

        assertEquals(List.of("reader"), result.roles());
        assertEquals(List.of(), result.permissions());
    }

    private UserPermission directAssignment(PermissionEffect effect) {
        UserPermission assignment = new UserPermission();
        assignment.setPermissionId(3L);
        assignment.setEffect(effect);
        return assignment;
    }

    private Permission permission(Long id, String name, boolean active, boolean system) {
        Permission permission = new Permission(); permission.setId(id); permission.setName(new PermissionName(name));
        permission.setActive(active); permission.setSystem(system); return permission;
    }
}
