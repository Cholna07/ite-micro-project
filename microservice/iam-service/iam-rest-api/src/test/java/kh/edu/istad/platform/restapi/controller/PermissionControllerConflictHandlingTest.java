package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.exception.PermissionNameConflictException;
import kh.edu.istad.platform.iam.domain.usecase.permission.*;
import kh.edu.istad.platform.restapi.dto.request.CreatePermissionRequest;
import kh.edu.istad.platform.restapi.exception.RestApiException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionControllerConflictHandlingTest {
    @Test
    void mapsApplicationAndDatabaseDuplicateConflictsToSanitized409() {
        CreatePermissionUseCase create = mock(CreatePermissionUseCase.class);
        PermissionController controller = controller(create);
        when(create.execute(any())).thenThrow(new PermissionNameConflictException());
        RestApiException applicationConflict = assertThrows(RestApiException.class,
                () -> controller.createPermission(new CreatePermissionRequest("user.read", "user", "read", null)));
        assertEquals(HttpStatus.CONFLICT, applicationConflict.getStatus());
        assertEquals("Permission name already exists", applicationConflict.getMessage());

        reset(create);
        when(create.execute(any())).thenThrow(new DataIntegrityViolationException("private database details"));
        RestApiException databaseConflict = assertThrows(RestApiException.class,
                () -> controller.createPermission(new CreatePermissionRequest("user.read", "user", "read", null)));
        assertEquals(HttpStatus.CONFLICT, databaseConflict.getStatus());
        assertFalse(databaseConflict.getMessage().contains("private database details"));
    }

    @Test
    void invalidPaginationIsBadRequest() {
        ListPermissionsUseCase list = mock(ListPermissionsUseCase.class);
        when(list.execute(any())).thenThrow(new IllegalArgumentException("Page must be non-negative"));
        PermissionController controller = new PermissionController(mock(CreatePermissionUseCase.class), list,
                mock(GetPermissionUseCase.class), mock(UpdatePermissionUseCase.class), mock(DeletePermissionUseCase.class),
                mock(ActivatePermissionUseCase.class), mock(DeactivatePermissionUseCase.class));
        RestApiException error = assertThrows(RestApiException.class,
                () -> controller.listPermissions(null, null, null, null, null, null, 0, 101, "name", "ASC"));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    private PermissionController controller(CreatePermissionUseCase create) {
        return new PermissionController(create, mock(ListPermissionsUseCase.class), mock(GetPermissionUseCase.class),
                mock(UpdatePermissionUseCase.class), mock(DeletePermissionUseCase.class),
                mock(ActivatePermissionUseCase.class), mock(DeactivatePermissionUseCase.class));
    }
}
