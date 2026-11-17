package kh.edu.istad.platform.iam.domain.usecase.config;

public class PasswordPolicyViolationException extends IllegalArgumentException {
    private final int minimumLength;
    private final int maximumLength;
    private final boolean tooLong;

    public PasswordPolicyViolationException(int minimumLength, int maximumLength, boolean tooLong, String message) {
        super(message);
        this.minimumLength = minimumLength;
        this.maximumLength = maximumLength;
        this.tooLong = tooLong;
    }

    public int minimumLength() { return minimumLength; }
    public int maximumLength() { return maximumLength; }
    public boolean tooLong() { return tooLong; }
}
