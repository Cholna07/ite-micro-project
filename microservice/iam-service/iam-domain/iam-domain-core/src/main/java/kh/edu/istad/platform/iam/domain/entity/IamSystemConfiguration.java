package kh.edu.istad.platform.iam.domain.entity;

public record IamSystemConfiguration(boolean registrationEnabled,
                                     int minimumPasswordLength,
                                     int maxFailedLoginAttempts) {
    public static final IamSystemConfiguration DEFAULTS = new IamSystemConfiguration(true, 8, 5);

    public String auditTargetId() { return "iam-system"; }
}
