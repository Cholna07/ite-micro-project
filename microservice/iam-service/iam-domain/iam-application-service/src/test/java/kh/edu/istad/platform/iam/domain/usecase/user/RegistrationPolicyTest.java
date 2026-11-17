package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.RegisterUserCommand;
import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.domain.port.out.SystemConfigurationRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.usecase.config.PasswordPolicy;
import kh.edu.istad.platform.iam.domain.usecase.config.RegistrationDisabledException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegistrationPolicyTest {
    @Test
    void disabledRegistrationRejectsBeforeCreatingAnAccount() {
        UserRepository users = mock(UserRepository.class);
        PasswordHasher hasher = mock(PasswordHasher.class);
        CurrentActorProvider actor = mock(CurrentActorProvider.class);
        PasswordPolicy policy = mock(PasswordPolicy.class);
        SystemConfigurationRepository config = mock(SystemConfigurationRepository.class);
        when(config.findCurrent()).thenReturn(new IamSystemConfiguration(false, 8, 5));
        RegisterUserUseCase useCase = new RegisterUserUseCase(users, hasher, actor, policy, config);

        assertThrows(RegistrationDisabledException.class,
                () -> useCase.execute(new RegisterUserCommand("alice", "alice@example.test", "password", null, null)));
        verifyNoInteractions(users, hasher, policy);
    }

    @Test
    void enabledRegistrationStillHashesAndCreatesTheUser() {
        UserRepository users = mock(UserRepository.class);
        PasswordHasher hasher = mock(PasswordHasher.class);
        CurrentActorProvider actor = mock(CurrentActorProvider.class);
        PasswordPolicy policy = mock(PasswordPolicy.class);
        SystemConfigurationRepository config = mock(SystemConfigurationRepository.class);
        when(config.findCurrent()).thenReturn(new IamSystemConfiguration(true, 8, 5));
        when(users.existsByUsername("alice")).thenReturn(false);
        when(users.existsByEmail("alice@example.test")).thenReturn(false);
        when(hasher.hash("password")).thenReturn("encoded");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RegisterUserUseCase useCase = new RegisterUserUseCase(users, hasher, actor, policy, config);

        var result = useCase.execute(new RegisterUserCommand("alice", "alice@example.test", "password", null, null));

        assertEquals("alice", result.username());
        verify(policy).validate("password");
        verify(hasher).hash("password");
        verify(users).save(any(User.class));
    }
}
