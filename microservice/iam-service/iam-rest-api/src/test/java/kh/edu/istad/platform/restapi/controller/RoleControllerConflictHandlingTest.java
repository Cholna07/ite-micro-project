package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.result.RoleResult;
import kh.edu.istad.platform.iam.domain.exception.RoleNameConflictException;
import kh.edu.istad.platform.iam.domain.usecase.role.ActivateRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.AssignPermissionsToRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.CreateRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.DeactivateRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.DeleteRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.GetRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.ListRolePermissionAssignmentsUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.ListRolesUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.RevokePermissionFromRoleUseCase;
import kh.edu.istad.platform.iam.domain.usecase.role.UpdateRoleUseCase;
import kh.edu.istad.platform.restapi.dto.request.CreateRoleRequest;
import kh.edu.istad.platform.iam.domain.exception.RolePermissionAlreadyAssignedException;
import kh.edu.istad.platform.restapi.exception.RestApiException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoleControllerConflictHandlingTest {
    @Test
    void roleNameDatabaseConflictsReturnSafeConflictDetails() {
        CreateRoleUseCase create = mock(CreateRoleUseCase.class);
        UpdateRoleUseCase update = mock(UpdateRoleUseCase.class);
        RoleController controller = controller(create, update, mock(AssignPermissionsToRoleUseCase.class));
        when(create.execute(any())).thenThrow(new DataIntegrityViolationException("internal constraint details"));

        RestApiException createError = assertThrows(RestApiException.class,
                () -> controller.createRole(new CreateRoleRequest("DUPLICATE", null)));
        assertEquals(HttpStatus.CONFLICT, createError.getStatus());
        assertEquals("Role name already exists", createError.getMessage());
        assertFalse(createError.getMessage().contains("internal"));

        UUID uuid = UUID.randomUUID();
        when(update.execute(any())).thenThrow(new RoleNameConflictException("Role name already exists"));
        RestApiException updateError = assertThrows(RestApiException.class,
                () -> controller.updateRole(uuid, new kh.edu.istad.platform.restapi.dto.request.UpdateRoleRequest("DUPLICATE", null)));
        assertEquals(HttpStatus.CONFLICT, updateError.getStatus());
    }

    @Test
    void rolePermissionDatabaseConflictsReturnHttp409WithoutDatabaseDetails() {
        AssignPermissionsToRoleUseCase assignment = mock(AssignPermissionsToRoleUseCase.class);
        RoleController controller = controller(mock(CreateRoleUseCase.class), mock(UpdateRoleUseCase.class), assignment);
        when(assignment.execute(any())).thenThrow(new DataIntegrityViolationException("internal index name"));

        RestApiException conflict = assertThrows(RestApiException.class,
                () -> controller.assignPermissions(UUID.randomUUID(),
                        new kh.edu.istad.platform.restapi.dto.request.AssignPermissionsToRoleRequest(java.util.List.of(UUID.randomUUID()))));

        assertEquals(HttpStatus.CONFLICT, conflict.getStatus());
        assertEquals("One or more permissions are already assigned to this role", conflict.getMessage());
    }

    private RoleController controller(CreateRoleUseCase create, UpdateRoleUseCase update, AssignPermissionsToRoleUseCase assignment) {
        return new RoleController(create, mock(ListRolesUseCase.class), mock(GetRoleUseCase.class), update,
                mock(DeleteRoleUseCase.class), mock(ActivateRoleUseCase.class), mock(DeactivateRoleUseCase.class),
                assignment, mock(RevokePermissionFromRoleUseCase.class), mock(ListRolePermissionAssignmentsUseCase.class));
    }
}
