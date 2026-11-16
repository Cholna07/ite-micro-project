package kh.edu.istad.platform.iam.domain.port.out;

import java.util.Collection;
import java.time.Instant;
import java.util.UUID;
import kh.edu.istad.platform.iam.domain.dto.result.IssuedAccessToken;

public interface AccessTokenProvider {

    IssuedAccessToken generateAccessToken(
            UUID userUuid,
            String username,
            Collection<String> permissions,
            UUID sessionId,
            Instant issuedAt
    );
}
