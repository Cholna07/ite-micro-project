package kh.edu.istad.platform.iam.domain.usecase.config;

import kh.edu.istad.platform.iam.domain.port.out.SystemConfigurationRepository;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicy {
    public static final int MAXIMUM_LENGTH = 100;
    private final SystemConfigurationRepository configurationRepository;

    public PasswordPolicy(SystemConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public int minimumLength() {
        return configurationRepository.findCurrent().minimumPasswordLength();
    }

    public void validate(String password) {
        int minimum = minimumLength();
        if (password == null || password.length() < minimum) {
            throw new PasswordPolicyViolationException(minimum, MAXIMUM_LENGTH, false,
                    "Password must contain at least " + minimum + " characters");
        }
        if (password.length() > MAXIMUM_LENGTH) {
            throw new PasswordPolicyViolationException(minimum, MAXIMUM_LENGTH, true,
                    "Password must contain no more than " + MAXIMUM_LENGTH + " characters");
        }
    }
}
