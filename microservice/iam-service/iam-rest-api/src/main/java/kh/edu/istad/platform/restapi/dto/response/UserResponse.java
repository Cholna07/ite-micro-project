package kh.edu.istad.platform.restapi.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(

        UUID uuid,
        String username,
        String email,
        String givenName,
        String familyName,
        String gender,
        LocalDate birthdate,
        String phoneNumber,
        String zoneInfo,
        String locale,
        AddressResponse address,
        boolean enabled,
        boolean locked,
        Instant createdAt,
        Instant updatedAt,
        String pictureUrl,
        String middleName,
        String nickname,
        boolean emailVerified,
        boolean phoneNumberVerified

) {
    public UserResponse(
            UUID uuid,
            String username,
            String email,
            String givenName,
            String familyName,
            String gender,
            LocalDate birthdate,
            String phoneNumber,
            String zoneInfo,
            String locale,
            AddressResponse address,
            boolean enabled,
            boolean locked,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(uuid, username, email, givenName, familyName, gender, birthdate, phoneNumber, zoneInfo, locale,
                address, enabled, locked, createdAt, updatedAt, null, null, null, false, false);
    }

    public UserResponse(
            UUID uuid,
            String username,
            String email,
            String givenName,
            String familyName,
            String gender,
            LocalDate birthdate,
            String phoneNumber,
            String zoneInfo,
            String locale,
            AddressResponse address,
            boolean enabled,
            boolean locked,
            Instant createdAt,
            Instant updatedAt,
            String pictureUrl
    ) {
        this(uuid, username, email, givenName, familyName, gender, birthdate, phoneNumber, zoneInfo, locale,
                address, enabled, locked, createdAt, updatedAt, pictureUrl, null, null, false, false);
    }

    public record AddressResponse(
            String streetAddress,
            String locality,
            String region,
            String postalCode,
            String country
    ) {
    }
}
