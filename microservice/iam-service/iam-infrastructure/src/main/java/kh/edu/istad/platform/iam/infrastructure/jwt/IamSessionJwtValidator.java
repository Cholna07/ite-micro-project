package kh.edu.istad.platform.iam.infrastructure.jwt;

import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.UUID;

public class IamSessionJwtValidator implements OAuth2TokenValidator<Jwt> {
    private static final String TOKEN_TYPE_CLAIM = "iam_token_type";
    private static final String SESSION_ID_CLAIM = "iam_session_id";
    private static final String CUSTOM_TOKEN_TYPE = "IAM_SESSION";

    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    public IamSessionJwtValidator(UserSessionRepository userSessionRepository, UserRepository userRepository) {
        this.userSessionRepository = userSessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        boolean looksLikeCustomIamToken = jwt.hasClaim("username") || jwt.hasClaim(TOKEN_TYPE_CLAIM);
        if (!looksLikeCustomIamToken) {
            return OAuth2TokenValidatorResult.success();
        }

        if (!CUSTOM_TOKEN_TYPE.equals(jwt.getClaimAsString(TOKEN_TYPE_CLAIM))) {
            return invalid("Custom IAM access token is missing its session marker");
        }

        UUID userUuid;
        UUID sessionId;
        try {
            userUuid = UUID.fromString(jwt.getSubject());
            sessionId = UUID.fromString(jwt.getClaimAsString(SESSION_ID_CLAIM));
        } catch (RuntimeException exception) {
            return invalid("Custom IAM access token has invalid session identifiers");
        }

        try {
            Instant now = Instant.now();
            if (!userSessionRepository.isActive(sessionId, userUuid, now)) {
                return invalid("Custom IAM access token session is inactive");
            }
            if (userRepository.findByUuid(userUuid)
                    .filter(user -> user.isEnabled() && !user.isDeleted() && user.isAccountNonLocked()).isEmpty()) {
                return invalid("Custom IAM access token user is inactive or locked");
            }
            return userSessionRepository.touchActivity(sessionId, userUuid, now)
                    ? OAuth2TokenValidatorResult.success()
                    : invalid("Custom IAM access token session is inactive");
        } catch (RuntimeException exception) {
            // A revocation-store failure must never make an IAM bearer token usable.
            return invalid("Custom IAM access token session could not be validated");
        }
    }

    private OAuth2TokenValidatorResult invalid(String description) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", description, null));
    }
}
