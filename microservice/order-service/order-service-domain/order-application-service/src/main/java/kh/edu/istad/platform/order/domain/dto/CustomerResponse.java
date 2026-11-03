package kh.edu.istad.platform.order.domain.dto;

public record CustomerResponse(
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber,
        String customerStatus
) {
}
