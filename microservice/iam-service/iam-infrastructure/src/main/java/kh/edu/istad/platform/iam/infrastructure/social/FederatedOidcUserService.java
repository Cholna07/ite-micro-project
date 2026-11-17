
package kh.edu.istad.platform.iam.infrastructure.social;

import kh.edu.istad.platform.iam.domain.dto.command.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.domain.dto.result.AuthUserResult;
import kh.edu.istad.platform.iam.domain.usecase.identity.FindOrProvisionFederatedUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.identity.LinkIdentityUseCase;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.UUID;

@Service
public class FederatedOidcUserService extends OidcUserService {

    private final FindOrProvisionFederatedUserUseCase
            findOrProvisionFederatedUserUseCase;
    private final LinkIdentityUseCase linkIdentityUseCase;
    private final UserRepository userRepository;

    public FederatedOidcUserService(
            FindOrProvisionFederatedUserUseCase
                    findOrProvisionFederatedUserUseCase,
            LinkIdentityUseCase linkIdentityUseCase,
            UserRepository userRepository
    ) {
        this.findOrProvisionFederatedUserUseCase =
                findOrProvisionFederatedUserUseCase;
        this.linkIdentityUseCase = linkIdentityUseCase;
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        String provider = userRequest.getClientRegistration()
                .getRegistrationId();

        // Telegram returns profile claims in its ID token and has no UserInfo endpoint.
        OidcUser oidcUser;
        if ("telegram".equalsIgnoreCase(provider)) {
            List<GrantedAuthority> tokenAuthorities = userRequest.getAccessToken()
                    .getScopes().stream()
                    .<GrantedAuthority>map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                    .toList();
            oidcUser = new DefaultOidcUser(
                    tokenAuthorities,
                    userRequest.getIdToken(),
                    "sub"
            );
        } else {
            oidcUser = super.loadUser(userRequest);
        }

        Map<String, Object> attributes = oidcUser.getAttributes();

        String providerSubject = oidcUser.getSubject();

        String email = oidcUser.getEmail();

        boolean emailVerified = Boolean.TRUE.equals(
                oidcUser.getEmailVerified()
        );

        String username;
        String displayName = oidcUser.getFullName();
        String pictureUrl = oidcUser.getPicture();

        if ("telegram".equalsIgnoreCase(provider)) {

            username = (String) attributes.get("preferred_username");

            if (username == null || username.isBlank()) {
                username = "telegram_" + providerSubject;
            }

            if (displayName == null || displayName.isBlank()) {
                displayName = username;
            }

        } else {

            username = (String) attributes.get("preferred_username");

            if (username == null || username.isBlank()) {
                username = email != null && email.contains("@")
                        ? email.substring(0, email.indexOf('@'))
                        : provider + "_" + providerSubject;
            }
        }

        // 3. Create federated identity command. Telegram does not return email.
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

        UUID pendingLinkUserUuid = IdentityLinkRequestContext.pendingUserUuid();
        if (pendingLinkUserUuid != null) {
            try {
                linkIdentityUseCase.execute(pendingLinkUserUuid, command);
            } catch (RuntimeException exception) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("identity_link_failed", "Social identity could not be linked", null),
                        exception);
            }
            String localUsername = userRepository.findByUuid(pendingLinkUserUuid)
                    .orElseThrow(() -> new IllegalStateException("Linking account is unavailable"))
                    .getUsername().value();
            return new IamOidcUser(oidcUser, localUsername);
        }

        // Ordinary social login continues through the existing find-or-provision flow.
        AuthUserResult authUser =
                findOrProvisionFederatedUserUseCase.execute(command);

        // 5. Check local IAM account status
        if (!authUser.enabled() || authUser.accountLocked()) {
            throw new IllegalStateException(
                    "IAM user account is disabled or locked"
            );
        }

        // 6. Combine provider and local IAM authorities
        Collection<GrantedAuthority> authorities =
                new ArrayList<>(oidcUser.getAuthorities());

        List<String> localAuthorities = new ArrayList<>();

        localAuthorities.addAll(authUser.authorities());
        localAuthorities.addAll(authUser.permissions());

        localAuthorities.stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        // 7. Return authenticated OIDC principal
        OidcUser authenticatedUser = new DefaultOidcUser(
                authorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "sub"
        );
        return new IamOidcUser(authenticatedUser, authUser.username());
    }
}
