package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePermissionRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 100)
        String resource,

        @NotBlank
        @Size(max = 100)
        String action,

        @Size(max = 500)
        String description

) {
}
