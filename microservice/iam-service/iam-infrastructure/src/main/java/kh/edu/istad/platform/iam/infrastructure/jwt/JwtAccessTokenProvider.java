package kh.edu.istad.platform.iam.infrastructure.jwt;

import kh.edu.istad.platform.iam.domain.port.out.AccessTokenProvider;
import kh.edu.istad.platform.iam.domain.dto.result.IssuedAccessToken;
import kh.edu.istad.platform.iam.infrastructure.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtAccessTokenProvider implements AccessTokenProvider {

    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    @Override
    public IssuedAccessToken generateAccessToken(
            UUID userUuid,
            String username,
            Collection<String> permissions,
            UUID sessionId,
            Instant issuedAt
    ) {
        Instant expiresAt = issuedAt.plus(jwtProperties.getAccessTokenExpiration());
        String value = jwtTokenService.generateAccessToken(
                userUuid,
                username,
                permissions,
                sessionId,
                issuedAt,
                expiresAt
        );
        return new IssuedAccessToken(value, expiresAt);
    }
}
