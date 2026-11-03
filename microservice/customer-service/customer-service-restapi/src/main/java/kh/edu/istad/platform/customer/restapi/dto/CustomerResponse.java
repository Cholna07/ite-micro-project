
package kh.edu.istad.platform.customer.restapi.dto;

public record CustomerResponse(
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber,
        String customerStatus
) {
}
