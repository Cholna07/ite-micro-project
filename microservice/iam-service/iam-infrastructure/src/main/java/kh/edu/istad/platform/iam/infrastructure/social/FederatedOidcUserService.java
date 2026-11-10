package kh.edu.istad.platform.iam.infrastructure.social;

import kh.edu.istad.platform.iam.domain.dto.command.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.domain.dto.result.AuthUserResult;
import kh.edu.istad.platform.iam.domain.usecase.identity.FindOrProvisionFederatedUserUseCase;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class FederatedOidcUserService extends OidcUserService {

    private final FindOrProvisionFederatedUserUseCase
            findOrProvisionFederatedUserUseCase;

    public FederatedOidcUserService(
            FindOrProvisionFederatedUserUseCase
                    findOrProvisionFederatedUserUseCase
    ) {
        this.findOrProvisionFederatedUserUseCase =
                findOrProvisionFederatedUserUseCase;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {

        // 1. Load the original OIDC user from Google.
        OidcUser oidcUser = super.loadUser(userRequest);

        Map<String, Object> attributes = oidcUser.getAttributes();

        // 2. Extract Google account information.
        String provider = userRequest.getClientRegistration()
                .getRegistrationId();

        String providerSubject = oidcUser.getSubject();

        String email = oidcUser.getEmail();

        boolean emailVerified = Boolean.TRUE.equals(
                oidcUser.getEmailVerified()
        );

        String username = (String) attributes.get("preferred_username");

        if (username == null || username.isBlank()) {
            username = email != null
                    ? email.substring(0, email.indexOf('@'))
                    : null;
        }

        String displayName = oidcUser.getFullName();

        String pictureUrl = oidcUser.getPicture();

        // 3. Create the federated identity command.
        FederatedIdentityCommand command =
                new FederatedIdentityCommand(
                        provider,
                        providerSubject,
                        email,
                        emailVerified,
                        username,
                        displayName,
                        pictureUrl,
                        attributes
                );

        // 4. Find or provision the local IAM user.
        AuthUserResult authUser =
                findOrProvisionFederatedUserUseCase.execute(command);

        // 5. Check local IAM account status.
        if (!authUser.enabled() || authUser.accountLocked()) {
            throw new IllegalStateException(
                    "IAM user account is disabled or locked"
            );
        }

        // 6. Combine Google authorities with local IAM authorities.
        Collection<GrantedAuthority> authorities =
                new ArrayList<>(oidcUser.getAuthorities());

        List<String> localAuthorities = new ArrayList<>();

        localAuthorities.addAll(authUser.authorities());
        localAuthorities.addAll(authUser.permissions());

        localAuthorities.stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        // 7. Return an authenticated OIDC principal.
        return new DefaultOidcUser(
                authorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "sub"
        );
    }
}
