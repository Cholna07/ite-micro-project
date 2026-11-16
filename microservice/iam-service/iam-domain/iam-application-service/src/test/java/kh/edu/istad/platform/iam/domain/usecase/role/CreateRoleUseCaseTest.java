package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.dto.command.CreateRoleCommand;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.exception.RoleNameConflictException;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateRoleUseCaseTest {
    @Test
    void createsRoleWithExistingNormalizationAndActiveDefaults() {
        RoleRepository repository = mock(RoleRepository.class);
        when(repository.existsByName("OPERATIONS")).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = new CreateRoleUseCase(repository).execute(new CreateRoleCommand(" operations ", "Operators"));

        assertEquals("OPERATIONS", result.name());
        assertTrue(result.active());
        verify(repository).save(argThat(role -> role.getUuid() != null && role.isActive() && !role.isDeleted()));
    }

    @Test
    void rejectsDuplicateAndBlankNamesBeforePersistence() {
        RoleRepository repository = mock(RoleRepository.class);
        when(repository.existsByName("ADMIN")).thenReturn(true);
        var useCase = new CreateRoleUseCase(repository);

        assertThrows(RoleNameConflictException.class, () -> useCase.execute(new CreateRoleCommand("admin", null)));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new CreateRoleCommand("  ", null)));
        verify(repository, never()).save(any());
    }
}
