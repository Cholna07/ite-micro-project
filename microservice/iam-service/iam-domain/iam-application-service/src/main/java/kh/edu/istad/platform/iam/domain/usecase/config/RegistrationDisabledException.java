package kh.edu.istad.platform.iam.domain.usecase.config;

public class RegistrationDisabledException extends RuntimeException {
    public RegistrationDisabledException() { super("Public account registration is currently disabled"); }
}
