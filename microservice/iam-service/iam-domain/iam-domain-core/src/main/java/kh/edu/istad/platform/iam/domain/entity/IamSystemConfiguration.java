package kh.edu.istad.platform.iam.domain.entity;

public record IamSystemConfiguration(boolean registrationEnabled,
                                     int minimumPasswordLength,
                                     int maxFailedLoginAttempts,
                                     String logoUrl) {
    public static final IamSystemConfiguration DEFAULTS = new IamSystemConfiguration(true, 8, 5, "");

    public IamSystemConfiguration(boolean registrationEnabled, int minimumPasswordLength, int maxFailedLoginAttempts) {
        this(registrationEnabled, minimumPasswordLength, maxFailedLoginAttempts, "");
    }

    public String auditTargetId() { return "iam-system"; }
}
