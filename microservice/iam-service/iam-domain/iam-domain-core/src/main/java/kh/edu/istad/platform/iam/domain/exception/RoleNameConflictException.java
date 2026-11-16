package kh.edu.istad.platform.iam.domain.exception;

public class RoleNameConflictException extends IllegalStateException {
    public RoleNameConflictException(String message) {
        super(message);
    }
}
