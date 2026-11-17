package kh.edu.istad.platform.iam.domain.usecase.config;

import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.SystemConfigurationRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SystemConfigurationUseCasesTest {
    @Test
    void validatesAndUpdatesTheAllowlistedValuesAndProducesSafeAuditDetails() {
        SystemConfigurationRepository repository = mock(SystemConfigurationRepository.class);
        CurrentActorProvider actor = mock(CurrentActorProvider.class);
        when(repository.findCurrent()).thenReturn(IamSystemConfiguration.DEFAULTS);
        when(actor.getCurrentActor()).thenReturn("admin-uuid");
        IamSystemConfiguration next = new IamSystemConfiguration(false, 12, 7);

        var result = new UpdateSystemConfigurationUseCase(repository, actor).execute(next);

        assertEquals(next, result.configuration());
        assertTrue(result.auditDetails().contains("registration.enabled"));
        assertTrue(result.auditDetails().contains("\"password.minimum-length\":12"));
        assertTrue(result.auditDetails().contains("\"authentication.max-failed-attempts\":7"));
        assertFalse(result.auditDetails().toLowerCase().contains("secret"));
        verify(repository).update(next, "admin-uuid");
    }

    @Test
    void rejectsOutOfRangeSettingsBeforeAnyPersistenceWrite() {
        SystemConfigurationRepository repository = mock(SystemConfigurationRepository.class);
        CurrentActorProvider actor = mock(CurrentActorProvider.class);
        UpdateSystemConfigurationUseCase useCase = new UpdateSystemConfigurationUseCase(repository, actor);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new IamSystemConfiguration(true, 7, 5)));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new IamSystemConfiguration(true, 33, 5)));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new IamSystemConfiguration(true, 8, 2)));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new IamSystemConfiguration(true, 8, 11)));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(
                new IamSystemConfiguration(true, 8, 5, "javascript:alert(1)")));
        verifyNoInteractions(repository, actor);
    }

    @Test
    void acceptsSecureLogoUrlAndDoesNotWriteItIntoAuditDetails() {
        SystemConfigurationRepository repository = mock(SystemConfigurationRepository.class);
        CurrentActorProvider actor = mock(CurrentActorProvider.class);
        when(repository.findCurrent()).thenReturn(IamSystemConfiguration.DEFAULTS);
        when(actor.getCurrentActor()).thenReturn("admin-uuid");
        String logo = "https://assets.example.edu/logo.png";

        var result = new UpdateSystemConfigurationUseCase(repository, actor)
                .execute(new IamSystemConfiguration(true, 8, 5, "  " + logo + "  "));

        assertEquals(logo, result.configuration().logoUrl());
        assertTrue(result.auditDetails().contains("\"branding.logo-configured\":true"));
        assertFalse(result.auditDetails().contains(logo));
    }

    @Test
    void passwordPolicyReadsTheCurrentMinimumAndEnforcesTheMaximum() {
        SystemConfigurationRepository repository = mock(SystemConfigurationRepository.class);
        when(repository.findCurrent()).thenReturn(new IamSystemConfiguration(true, 12, 5));
        PasswordPolicy policy = new PasswordPolicy(repository);
        policy.validate("123456789012");
        var shortPassword = assertThrows(PasswordPolicyViolationException.class, () -> policy.validate("12345678"));
        assertEquals(12, shortPassword.minimumLength());
        var longPassword = assertThrows(PasswordPolicyViolationException.class, () -> policy.validate("x".repeat(101)));
        assertTrue(longPassword.tooLong());
        assertEquals(100, longPassword.maximumLength());
        verify(repository, times(3)).findCurrent();
    }
}
