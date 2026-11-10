
package kh.edu.istad.platform.iam.infrastructure.oauth2;

import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class OidcInfoService
        implements Function<OidcUserInfoAuthenticationContext, OidcUserInfo> {

    private final UserRepository userRepository;
    private final OidcClaimMapper oidcClaimMapper;

    public OidcInfoService(
            UserRepository userRepository,
            OidcClaimMapper oidcClaimMapper
    ) {
        this.userRepository = userRepository;
        this.oidcClaimMapper = oidcClaimMapper;
    }

    @Override
    public OidcUserInfo apply(
            OidcUserInfoAuthenticationContext context
    ) {
        String username = context.getAuthorization().getPrincipalName();

        User user = userRepository
                .findByUsernameOrEmail(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (user.isDeleted() || !user.isEnabled()) {
            throw new IllegalStateException(
                    "User account is unavailable"
            );
        }

        return oidcClaimMapper.toUserInfo(user);
    }
}
