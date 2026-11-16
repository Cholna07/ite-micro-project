package kh.edu.istad.platform.iam.domain.exception;

public class PermissionNameConflictException extends RuntimeException {
    public PermissionNameConflictException() { super("Permission name already exists"); }
}
