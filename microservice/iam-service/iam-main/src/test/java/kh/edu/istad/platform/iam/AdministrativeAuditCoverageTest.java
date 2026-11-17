package kh.edu.istad.platform.iam;

import kh.edu.istad.platform.iam.domain.audit.AuditAction;
import kh.edu.istad.platform.iam.domain.usecase.permission.*;
import kh.edu.istad.platform.iam.domain.usecase.role.*;
import kh.edu.istad.platform.iam.domain.usecase.user.*;
import kh.edu.istad.platform.iam.infrastructure.oauth2.OAuthClientManagementService;
import kh.edu.istad.platform.restapi.controller.AdminUserSessionController;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdministrativeAuditCoverageTest {
    @Test
    void administrativeUseCasesAreInstrumentedAcrossResourceTypes() {
        Map<Class<?>, String[]> expected = Map.ofEntries(
                Map.entry(CreateUserUseCase.class, new String[]{"USER_CREATED"}),
                Map.entry(UpdateUserProfileUseCase.class, new String[]{"USER_UPDATED"}),
                Map.entry(DeleteUserUseCase.class, new String[]{"USER_DELETED"}),
                Map.entry(EnableUserUseCase.class, new String[]{"USER_ENABLED"}),
                Map.entry(DisableUserUseCase.class, new String[]{"USER_DISABLED"}),
                Map.entry(LockUserUseCase.class, new String[]{"USER_LOCKED"}),
                Map.entry(UnlockUserUseCase.class, new String[]{"USER_UNLOCKED"}),
                Map.entry(ResetUserPasswordUseCase.class, new String[]{"USER_PASSWORD_RESET"}),
                Map.entry(AssignRolesToUserUseCase.class, new String[]{"USER_ROLE_ASSIGNED"}),
                Map.entry(RevokeRoleFromUserUseCase.class, new String[]{"USER_ROLE_REVOKED"}),
                Map.entry(GrantPermissionsToUserUseCase.class, new String[]{"USER_PERMISSION_GRANTED"}),
                Map.entry(RevokePermissionFromUserUseCase.class, new String[]{"USER_PERMISSION_REVOKED"}),
                Map.entry(CreateRoleUseCase.class, new String[]{"ROLE_CREATED"}),
                Map.entry(UpdateRoleUseCase.class, new String[]{"ROLE_UPDATED"}),
                Map.entry(DeleteRoleUseCase.class, new String[]{"ROLE_DELETED"}),
                Map.entry(ActivateRoleUseCase.class, new String[]{"ROLE_ACTIVATED"}),
                Map.entry(DeactivateRoleUseCase.class, new String[]{"ROLE_DEACTIVATED"}),
                Map.entry(AssignPermissionsToRoleUseCase.class, new String[]{"ROLE_PERMISSIONS_ASSIGNED"}),
                Map.entry(RevokePermissionFromRoleUseCase.class, new String[]{"ROLE_PERMISSION_REVOKED"}),
                Map.entry(CreatePermissionUseCase.class, new String[]{"PERMISSION_CREATED"}),
                Map.entry(UpdatePermissionUseCase.class, new String[]{"PERMISSION_UPDATED"}),
                Map.entry(DeletePermissionUseCase.class, new String[]{"PERMISSION_DELETED"}),
                Map.entry(ActivatePermissionUseCase.class, new String[]{"PERMISSION_ACTIVATED"}),
                Map.entry(DeactivatePermissionUseCase.class, new String[]{"PERMISSION_DEACTIVATED"}),
                Map.entry(OAuthClientManagementService.class, new String[]{"CLIENT_CREATED", "CLIENT_UPDATED", "CLIENT_ACTIVATED", "CLIENT_DEACTIVATED", "CLIENT_SECRET_ROTATED"}),
                Map.entry(AdminUserSessionController.class, new String[]{"SESSION_REVOKED", "SESSIONS_REVOKED"})
        );
        expected.forEach((type, actions) -> {
            for (String action : actions) {
                assertTrue(java.util.Arrays.stream(type.getDeclaredMethods())
                                .map(method -> method.getAnnotation(AuditAction.class))
                                .filter(java.util.Objects::nonNull)
                                .anyMatch(annotation -> action.equals(annotation.action())),
                        () -> type.getSimpleName() + " must record " + action);
            }
        });
    }

    @Test
    void migrationCreatesAppendOnlyHistoryWithoutCascadingActorOrTargetReferences() throws Exception {
        String migration = new String(getClass().getResourceAsStream("/changelog/ddl/changelog-0017.sql").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).toLowerCase();
        String master = new String(getClass().getResourceAsStream("/changelog/db.changelog-master.yaml").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(migration.contains("create table admin_audit_events"));
        assertFalse(migration.contains("foreign key"));
        assertFalse(migration.contains("on delete cascade"));
        assertTrue(migration.contains("idx_admin_audit_events_actor_time"));
        assertTrue(migration.contains("idx_admin_audit_events_target_time"));
        assertTrue(migration.contains("idx_admin_audit_events_action_time"));
        assertTrue(migration.contains("idx_admin_audit_events_outcome_time"));
        assertTrue(master.contains("path: ddl"));
        assertTrue(migration.contains("'audit.read'"));
    }
}
