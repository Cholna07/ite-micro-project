package kh.edu.istad.platform.iam.domain.exception;

public class RolePermissionAlreadyAssignedException extends IllegalStateException {
    public RolePermissionAlreadyAssignedException(String message) {
        super(message);
    }
}
