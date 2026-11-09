package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record CreateUserRequest(

        @NotBlank
        @Size(max = 100)
        String username,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        String password,

        @Size(max = 100)
        String givenName,

        @Size(max = 100)
        String familyName,
        String middleName,

        String gender,

        LocalDate birthdate,

        String phoneNumber,

        String zoneInfo,

        String locale,

        @Valid
        AddressRequest address,

        Set<UUID> roleUuids

) {

    public record AddressRequest(
            String streetAddress,
            String locality,
            String region,
            String postalCode,
            String country
    ) {
    }
}
