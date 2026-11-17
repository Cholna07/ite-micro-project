package kh.edu.istad.platform.iam.domain.usecase.user;

/** Raised when a registration username or email is already in use. */
public class DuplicateAccountException extends RuntimeException {
    public DuplicateAccountException() {
        super("Username or email is already in use");
    }
}
