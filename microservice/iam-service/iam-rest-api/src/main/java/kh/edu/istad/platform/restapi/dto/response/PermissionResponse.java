
package kh.edu.istad.platform.restapi.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PermissionResponse(

        UUID uuid,
        String name,
        String resource,
        String action,
        String description,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt

) {
}
