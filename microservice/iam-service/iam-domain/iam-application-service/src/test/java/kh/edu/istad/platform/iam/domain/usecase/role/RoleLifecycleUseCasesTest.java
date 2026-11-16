package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleLifecycleUseCasesTest {
    @Test
    void deactivatesAndReactivatesRolesAndPreventsDeletedRoleActivation() {
        UUID uuid = UUID.randomUUID(); Role role = role(uuid, false);
        RoleRepository repository = mock(RoleRepository.class);
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(role));

        new DeactivateRoleUseCase(repository).execute(uuid);
        assertFalse(role.isActive());
        new ActivateRoleUseCase(repository).execute(uuid);
        assertTrue(role.isActive());
        role.setDeleted(true);
        assertThrows(IllegalStateException.class, () -> new ActivateRoleUseCase(repository).execute(uuid));
        verify(repository, times(2)).save(role);
    }

    @Test
    void softDeletesRoleAndRejectsSystemRoleDeletion() {
        UUID uuid = UUID.randomUUID(); Role role = role(uuid, false);
        Role systemRole = role(UUID.randomUUID(), true);
        RoleRepository repository = mock(RoleRepository.class);
        CurrentActorProvider actors = mock(CurrentActorProvider.class);
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(role));
        when(repository.findByUuid(systemRole.getUuid())).thenReturn(Optional.of(systemRole));
        when(actors.getCurrentActor()).thenReturn("admin");

        new DeleteRoleUseCase(repository, actors).execute(uuid);
        assertTrue(role.isDeleted());
        assertFalse(role.isActive());
        assertNotNull(role.getDeletedAt());
        assertEquals("admin", role.getDeletedBy());
        assertThrows(IllegalStateException.class, () -> new DeleteRoleUseCase(repository, actors).execute(systemRole.getUuid()));
        verify(repository).save(role);
        verify(repository, never()).save(systemRole);
    }

    private Role role(UUID uuid, boolean system) {
        Role role = new Role(); role.setId(1L); role.setUuid(uuid); role.setName(new RoleName("SAMPLE")); role.setActive(true); role.setSystem(system); return role;
    }
}
