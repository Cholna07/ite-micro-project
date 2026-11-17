package kh.edu.istad.platform.iam.domain.usecase.user;

public class CurrentPasswordIncorrectException extends IllegalArgumentException {
    public CurrentPasswordIncorrectException() {
        super("Current password is incorrect");
    }
}
