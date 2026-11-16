package kh.edu.istad.platform.iam.infrastructure.jwt;

import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.OAuth2Token;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(
            UUID userUuid,
            String username,
            Collection<String> permissions,
            UUID sessionId,
            Instant issuedAt,
            Instant expiresAt
    ) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(userUuid.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("username", username)
                .claim("permissions", permissions)
                .claim("iam_token_type", "IAM_SESSION")
                .claim("iam_session_id", sessionId.toString())
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .build();

        OAuth2Token token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        );

        return token.getTokenValue();
    }
}
