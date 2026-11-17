package kh.edu.istad.platform.iam.domain.dto.result;

import java.time.Instant;
import java.util.UUID;

public record UserResult(
        UUID uuid,
        String username,
        String email,
        String givenName,
        String familyName,
        String middleName,
        String nickname,
        String gender,
        String phoneNumber,
        boolean emailVerified,
        boolean phoneNumberVerified,
        boolean enabled,
        boolean accountLocked,
        Instant createdAt,
        Instant updatedAt,
        String pictureUrl,
        Instant passwordChangedAt,
        int failedLoginAttempts
) {
    public UserResult(
            UUID uuid,
            String username,
            String email,
            String givenName,
            String familyName,
            String middleName,
            String nickname,
            String gender,
            String phoneNumber,
            boolean emailVerified,
            boolean phoneNumberVerified,
            boolean enabled,
            boolean accountLocked,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(uuid, username, email, givenName, familyName, middleName, nickname, gender, phoneNumber,
                emailVerified, phoneNumberVerified, enabled, accountLocked, createdAt, updatedAt, null, null, 0);
    }

    public UserResult(UUID uuid, String username, String email, String givenName, String familyName,
                      String middleName, String nickname, String gender, String phoneNumber,
                      boolean emailVerified, boolean phoneNumberVerified, boolean enabled,
                      boolean accountLocked, Instant createdAt, Instant updatedAt, String pictureUrl) {
        this(uuid, username, email, givenName, familyName, middleName, nickname, gender, phoneNumber,
                emailVerified, phoneNumberVerified, enabled, accountLocked, createdAt, updatedAt,
                pictureUrl, null, 0);
    }
}
