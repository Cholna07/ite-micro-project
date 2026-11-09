package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetUserPasswordRequest(

        @NotBlank
        @Size(min = 8, max = 100)
        String newPassword,

        @NotBlank
        String confirmPassword

) {
}
