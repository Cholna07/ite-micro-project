package kh.edu.istad.platform.iam.domain.dto.command;

import java.util.Map;

public record FederatedIdentityCommand(
        String provider,
        String providerSubject,
        String email,
        boolean emailVerified,
        String username,
        String displayName,
        String pictureUrl,
        Map<String, Object> attributes
) {
}
