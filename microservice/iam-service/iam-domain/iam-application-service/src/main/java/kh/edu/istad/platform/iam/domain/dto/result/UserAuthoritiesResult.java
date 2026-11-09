package kh.edu.istad.platform.iam.domain.dto.result;

import java.util.List;
import java.util.UUID;

public record UserAuthoritiesResult(
        UUID userUuid,
        List<String> roles,
        List<String> permissions
) {
}
