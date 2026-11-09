
package kh.edu.istad.platform.restapi.dto.response;

import java.util.Set;
import java.util.UUID;

public record UserAuthoritiesResponse(
        UUID userUuid,
        String username,
        Set<String> roles,
        Set<String> permissions
) {
}
