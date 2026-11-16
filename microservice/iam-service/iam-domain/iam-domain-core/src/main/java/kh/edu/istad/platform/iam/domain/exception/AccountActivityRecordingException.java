package kh.edu.istad.platform.iam.domain.exception;

public class AccountActivityRecordingException extends RuntimeException {
    public AccountActivityRecordingException(Throwable cause) {
        super("Account activity could not be recorded", cause);
    }
}
