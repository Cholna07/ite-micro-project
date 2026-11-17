package kh.edu.istad.platform.iam.domain.usecase.user;

/** Raised when authentication is denied because an account is locked or disabled. */
public class AccountUnavailableException extends RuntimeException {
    public AccountUnavailableException(String message) {
        super(message);
    }
}
