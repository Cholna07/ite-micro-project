package kh.edu.istad.platform.customer.restapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CustomerCreateRequest(
        @NotBlank
        String username,

        @NotBlank
        String familyName,

        @NotBlank
        String givenName,

        @NotBlank
        String email,

        @NotBlank
        String phoneNumber,

        @NotNull
        UUID segmentId
) {
}