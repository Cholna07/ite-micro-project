package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

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

        @NotBlank
        String confirmPassword,

        @Size(max = 100)
        String givenName,

        @Size(max = 100)
        String familyName,

        @Size(max = 20)
        String locale

) {
}
