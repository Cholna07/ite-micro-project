package kh.edu.istad.platform.iam.domain.exception;

public class RoleAlreadyAssignedException extends IllegalArgumentException {
    public RoleAlreadyAssignedException(String message) {
        super(message);
    }
}
