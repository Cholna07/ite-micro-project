package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;

public record UpdateSystemConfigurationRequest(
        @NotNull Boolean registrationEnabled,
        @NotNull @Min(8) @Max(32) Integer minimumPasswordLength,
        @NotNull @Min(3) @Max(10) Integer maxFailedLoginAttempts
) {
    public IamSystemConfiguration toConfiguration() {
        return new IamSystemConfiguration(registrationEnabled, minimumPasswordLength, maxFailedLoginAttempts);
    }
}
