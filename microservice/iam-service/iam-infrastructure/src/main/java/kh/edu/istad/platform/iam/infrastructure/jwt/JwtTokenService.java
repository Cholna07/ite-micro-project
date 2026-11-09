package kh.edu.istad.platform.iam.infrastructure.jwt;

import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
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
            Collection<String> permissions
    ) {
        Instant now = Instant.now();

        Instant expiresAt = now.plus(
                jwtProperties.getAccessTokenExpiration()
        );

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(userUuid.toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("username", username)
                .claim("permissions", permissions)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .build();

        OAuth2Token token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        );

        return token.getTokenValue();
    }
}