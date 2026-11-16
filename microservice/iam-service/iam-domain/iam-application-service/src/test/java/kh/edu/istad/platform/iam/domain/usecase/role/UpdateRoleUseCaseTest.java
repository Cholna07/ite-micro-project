package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.dto.command.UpdateRoleCommand;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.exception.RoleNameConflictException;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateRoleUseCaseTest {
    @Test
    void updatesRoleAndNormalizesName() {
        UUID uuid = UUID.randomUUID();
        Role role = role(uuid, "SUPPORT", false);
        RoleRepository repository = mock(RoleRepository.class);
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(role));
        when(repository.save(role)).thenReturn(role);

        var result = new UpdateRoleUseCase(repository).execute(new UpdateRoleCommand(uuid, " support 2 ", "New description"));

        assertEquals("SUPPORT 2", result.name());
        assertEquals("New description", result.description());
        verify(repository).save(role);
    }

    @Test
    void rejectsDuplicateNamesInvalidNamesAndSystemRoleModification() {
        UUID uuid = UUID.randomUUID();
        Role role = role(uuid, "SUPPORT", false);
        Role system = role(UUID.randomUUID(), "ADMIN", true);
        RoleRepository repository = mock(RoleRepository.class);
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(role));
        when(repository.findByUuid(system.getUuid())).thenReturn(Optional.of(system));
        when(repository.existsByName("TAKEN")).thenReturn(true);
        var useCase = new UpdateRoleUseCase(repository);

        assertThrows(RoleNameConflictException.class, () -> useCase.execute(new UpdateRoleCommand(uuid, "taken", null)));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new UpdateRoleCommand(uuid, " ", null)));
        assertThrows(IllegalStateException.class, () -> useCase.execute(new UpdateRoleCommand(system.getUuid(), "ADMIN2", null)));
        verify(repository, never()).save(any());
    }

    private Role role(UUID uuid, String name, boolean system) {
        Role role = new Role(); role.setId(1L); role.setUuid(uuid); role.setName(new RoleName(name)); role.setActive(true); role.setSystem(system); return role;
    }
}
