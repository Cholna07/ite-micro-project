package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;

public record SystemConfigurationResponse(boolean registrationEnabled, int minimumPasswordLength,
                                          int maxFailedLoginAttempts) {
    public static SystemConfigurationResponse from(IamSystemConfiguration value) {
        return new SystemConfigurationResponse(value.registrationEnabled(), value.minimumPasswordLength(),
                value.maxFailedLoginAttempts());
    }
}
