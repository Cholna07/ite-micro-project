package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.RegisterUserCommand;
import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.port.out.SystemConfigurationRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRoleRepository;
import kh.edu.istad.platform.iam.domain.usecase.config.PasswordPolicy;
import kh.edu.istad.platform.iam.domain.usecase.config.RegistrationDisabledException;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.UUID;

class RegistrationPolicyTest {
    @Test
    void disabledRegistrationRejectsBeforeCreatingAnAccount() {
        UserRepository users = mock(UserRepository.class);
        PasswordHasher hasher = mock(PasswordHasher.class);
        CurrentActorProvider actor = mock(CurrentActorProvider.class);
        PasswordPolicy policy = mock(PasswordPolicy.class);
        SystemConfigurationRepository config = mock(SystemConfigurationRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserRoleRepository userRoles = mock(UserRoleRepository.class);
        when(config.findCurrent()).thenReturn(new IamSystemConfiguration(false, 8, 5));
        RegisterUserUseCase useCase = new RegisterUserUseCase(users, hasher, actor, policy, config, roles, userRoles);

        assertThrows(RegistrationDisabledException.class,
                () -> useCase.execute(new RegisterUserCommand("alice", "alice@example.test", "password", null, null)));
        verifyNoInteractions(users, hasher, policy);
        verifyNoInteractions(roles, userRoles);
    }

    @Test
    void enabledRegistrationStillHashesAndCreatesTheUser() {
        UserRepository users = mock(UserRepository.class);
        PasswordHasher hasher = mock(PasswordHasher.class);
        CurrentActorProvider actor = mock(CurrentActorProvider.class);
        PasswordPolicy policy = mock(PasswordPolicy.class);
        SystemConfigurationRepository config = mock(SystemConfigurationRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserRoleRepository userRoles = mock(UserRoleRepository.class);
        when(config.findCurrent()).thenReturn(new IamSystemConfiguration(true, 8, 5));
        when(users.existsByUsername("alice")).thenReturn(false);
        when(users.existsByEmail("alice@example.test")).thenReturn(false);
        when(hasher.hash("password")).thenReturn("encoded");
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(5L);
            return user;
        });
        Role userRole = role(7L, "USER");
        when(roles.findByName("USER")).thenReturn(Optional.of(userRole));
        RegisterUserUseCase useCase = new RegisterUserUseCase(users, hasher, actor, policy, config, roles, userRoles);

        var result = useCase.execute(new RegisterUserCommand("alice", "alice@example.test", "password", null, null));

        assertEquals("alice", result.username());
        verify(policy).validate("password");
        verify(hasher).hash("password");
        verify(users).save(any(User.class));
        verify(userRoles).save(argThat(assignment -> assignment.getUserId().equals(5L)
                && assignment.getRoleId().equals(7L)
                && "SYSTEM".equals(assignment.getAssignedBy())));
    }

    @Test
    void duplicateUsernameIsReportedAsAnAccountConflictBeforeHashing() {
        UserRepository users = mock(UserRepository.class);
        PasswordHasher hasher = mock(PasswordHasher.class);
        CurrentActorProvider actor = mock(CurrentActorProvider.class);
        PasswordPolicy policy = mock(PasswordPolicy.class);
        SystemConfigurationRepository config = mock(SystemConfigurationRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserRoleRepository userRoles = mock(UserRoleRepository.class);
        when(config.findCurrent()).thenReturn(new IamSystemConfiguration(true, 8, 5));
        when(users.existsByUsername("alice")).thenReturn(true);
        RegisterUserUseCase useCase = new RegisterUserUseCase(users, hasher, actor, policy, config, roles, userRoles);

        assertThrows(DuplicateAccountException.class,
                () -> useCase.execute(new RegisterUserCommand("alice", "alice@example.test", "password", null, null)));
        verify(policy).validate("password");
        verifyNoInteractions(hasher, actor);
        verify(users, never()).save(any(User.class));
        verifyNoInteractions(userRoles);
    }

    private Role role(long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setUuid(UUID.randomUUID());
        role.setName(new RoleName(name));
        role.setActive(true);
        return role;
    }
}
