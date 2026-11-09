package kh.edu.istad.platform.iam.domain.port.out;

import java.util.Collection;
import java.util.UUID;

public interface AccessTokenProvider {

    String generateAccessToken(
            UUID userUuid,
            String username,
            Collection<String> permissions
    );
}