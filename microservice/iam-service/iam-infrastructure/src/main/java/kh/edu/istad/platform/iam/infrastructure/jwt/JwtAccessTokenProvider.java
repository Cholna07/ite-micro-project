package kh.edu.istad.platform.iam.infrastructure.jwt;

import kh.edu.istad.platform.iam.domain.port.out.AccessTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAccessTokenProvider implements AccessTokenProvider {

    private final JwtTokenService jwtTokenService;

    @Override
    public String generateAccessToken(
            UUID userUuid,
            String username,
            Collection<String> permissions
    ) {
        return jwtTokenService.generateAccessToken(
                userUuid,
                username,
                permissions
        );
    }
}