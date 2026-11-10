package kh.edu.istad.platform.iam.infrastructure.oauth2;

import kh.edu.istad.platform.iam.domain.entity.User;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OidcClaimMapper {

    public OidcUserInfo toUserInfo(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("sub", user.getUuid().toString());
        claims.put("preferred_username", user.getUsername().value());
        claims.put("email", user.getEmail().value());
        claims.put("email_verified", user.isEmailVerified());

        addIfPresent(claims, "given_name", user.getGivenName());
        addIfPresent(claims, "family_name", user.getFamilyName());
        addIfPresent(claims, "middle_name", user.getMiddleName());
        addIfPresent(claims, "nickname", user.getNickname());
        addIfPresent(claims, "picture", user.getPictureUrl());
        addIfPresent(claims, "profile", user.getProfileUrl());
        addIfPresent(claims, "website", user.getWebsiteUrl());
        addIfPresent(claims, "locale", user.getLocale());
        addIfPresent(claims, "zoneinfo", user.getZoneInfo());

        return new OidcUserInfo(claims);
    }

    private void addIfPresent(
            Map<String, Object> claims,
            String key,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            claims.put(key, value);
        }
    }
}
