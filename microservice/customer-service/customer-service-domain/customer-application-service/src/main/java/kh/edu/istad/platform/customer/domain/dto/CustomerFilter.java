
package kh.edu.istad.platform.customer.domain.dto;

import java.util.UUID;

public record CustomerFilter(
        String username,
        String familyName,
        String givenName,
        String email,
        String status,
        UUID segmentId
) {
}
