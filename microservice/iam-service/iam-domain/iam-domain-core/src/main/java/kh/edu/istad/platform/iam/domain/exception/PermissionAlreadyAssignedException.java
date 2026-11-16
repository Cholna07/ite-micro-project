package kh.edu.istad.platform.iam.domain.exception;

public class PermissionAlreadyAssignedException extends IllegalStateException {
    public PermissionAlreadyAssignedException(String message) {
        super(message);
    }
}
