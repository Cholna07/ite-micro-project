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
        Instant updatedAt

) {
    public record AddressResponse(
            String streetAddress,
            String locality,
            String region,
            String postalCode,
            String country
    ) {
    }
}
