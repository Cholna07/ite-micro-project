package kh.edu.istad.platform.iam.domain.usecase.config;

import kh.edu.istad.platform.iam.domain.audit.AuditAction;
import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.SystemConfigurationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateSystemConfigurationUseCase {
    private final SystemConfigurationRepository repository;
    private final CurrentActorProvider actorProvider;

    public UpdateSystemConfigurationUseCase(SystemConfigurationRepository repository, CurrentActorProvider actorProvider) {
        this.repository = repository;
        this.actorProvider = actorProvider;
    }

    @Transactional
    @AuditAction(action = "SYSTEM_CONFIGURATION_UPDATED", targetType = "SYSTEM_CONFIGURATION",
            targetArgumentIndex = 0, targetProperty = "auditTargetId", detailsProperty = "auditDetails")
    public UpdateSystemConfigurationResult execute(IamSystemConfiguration requested) {
        if (requested == null) throw new IllegalArgumentException("System configuration is required");
        validate(requested);
        IamSystemConfiguration before = repository.findCurrent();
        repository.update(requested, actorProvider.getCurrentActor());
        return new UpdateSystemConfigurationResult(requested, safeAuditDetails(before, requested));
    }

    private void validate(IamSystemConfiguration value) {
        if (value.minimumPasswordLength() < 8 || value.minimumPasswordLength() > 32)
            throw new IllegalArgumentException("Minimum password length must be between 8 and 32");
        if (value.maxFailedLoginAttempts() < 3 || value.maxFailedLoginAttempts() > 10)
            throw new IllegalArgumentException("Maximum failed login attempts must be between 3 and 10");
    }

    private String safeAuditDetails(IamSystemConfiguration before, IamSystemConfiguration after) {
        return "{\"before\":{\"registration.enabled\":" + before.registrationEnabled()
                + ",\"password.minimum-length\":" + before.minimumPasswordLength()
                + ",\"authentication.max-failed-attempts\":" + before.maxFailedLoginAttempts()
                + "},\"after\":{\"registration.enabled\":" + after.registrationEnabled()
                + ",\"password.minimum-length\":" + after.minimumPasswordLength()
                + ",\"authentication.max-failed-attempts\":" + after.maxFailedLoginAttempts() + "}}";
    }
}
