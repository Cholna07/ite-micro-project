
package kh.edu.istad.platform.iam.infrastructure.social;

import kh.edu.istad.platform.iam.domain.dto.command.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.domain.dto.result.AuthUserResult;
import kh.edu.istad.platform.iam.domain.usecase.identity.FindOrProvisionFederatedUserUseCase;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class FederatedOAuth2UserService extends DefaultOAuth2UserService {

    private final FindOrProvisionFederatedUserUseCase
            findOrProvisionFederatedUserUseCase;

    private final RestClient restClient;

    public FederatedOAuth2UserService(
            FindOrProvisionFederatedUserUseCase
                    findOrProvisionFederatedUserUseCase,
            RestClient.Builder restClientBuilder
    ) {
        this.findOrProvisionFederatedUserUseCase =
                findOrProvisionFederatedUserUseCase;

        this.restClient = restClientBuilder.build();
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        // 1. Load user information from OAuth2 provider
        OAuth2User oauth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oauth2User.getAttributes();

        String provider = userRequest.getClientRegistration()
                .getRegistrationId();

        // 2. Extract provider-specific information
        String providerSubject =
                String.valueOf(attributes.get("id"));

        String email = (String) attributes.get("email");
        boolean emailVerified = false;

        String displayName = (String) attributes.get("name");
        String username;
        String pictureUrl;

        // 3. Handle GitHub profile
        if ("github".equalsIgnoreCase(provider)) {

            username = (String) attributes.get("login");
            pictureUrl = (String) attributes.get("avatar_url");

            // Retrieve verified email from GitHub if necessary
            List<Map<String, Object>> emails =
                    restClient.get()
                            .uri("https://api.github.com/user/emails")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    "Bearer " + userRequest.getAccessToken()
                                            .getTokenValue()
                            )
                            .header(HttpHeaders.ACCEPT,
                                    "application/vnd.github+json")
                            .retrieve()
                            .body(new ParameterizedTypeReference<>() {});

            if (emails != null) {

                Map<String, Object> selectedEmail = emails.stream()
                        .filter(item ->
                                Boolean.TRUE.equals(item.get("primary"))
                                        && Boolean.TRUE.equals(
                                        item.get("verified")
                                )
                        )
                        .findFirst()
                        .orElseGet(() -> emails.stream()
                                .filter(item ->
                                        Boolean.TRUE.equals(
                                                item.get("verified")
                                        )
                                )
                                .findFirst()
                                .orElse(null));

                if (selectedEmail != null) {
                    email = (String) selectedEmail.get("email");
                    emailVerified = true;
                }
            }

        } else {

            // 4. Handle Facebook profile
            username = (String) attributes.get("username");

            if (username == null || username.isBlank()) {
                if (email != null && email.contains("@")) {
                    username = email.substring(0, email.indexOf('@'));
                } else {
                    username = provider + "_" + providerSubject;
                }
            }

            // Facebook picture is a nested object
            pictureUrl = null;

            Object pictureObject = attributes.get("picture");

            if (pictureObject instanceof Map<?, ?> pictureMap) {

                Object dataObject = pictureMap.get("data");

                if (dataObject instanceof Map<?, ?> dataMap) {
                    pictureUrl = (String) dataMap.get("url");
                }
            }

            // Facebook does not provide a verified-email claim here
            emailVerified = false;
        }

        // 5. Apply a fallback username
        if (username == null || username.isBlank()) {
            username = provider + "_" + providerSubject;
        }

        // 6. Create federated identity command
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

        // 7. Find or provision local IAM user
        AuthUserResult authUser =
                findOrProvisionFederatedUserUseCase.execute(command);

        // 8. Check local IAM account status
        if (!authUser.enabled() || authUser.accountLocked()) {
            throw new IllegalStateException(
                    "IAM user account is disabled or locked"
            );
        }

        // 9. Combine provider and local IAM authorities
        Collection<GrantedAuthority> authorities =
                new ArrayList<>(oauth2User.getAuthorities());

        List<String> localAuthorities = new ArrayList<>();

        localAuthorities.addAll(authUser.authorities());
        localAuthorities.addAll(authUser.permissions());

        localAuthorities.stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        // 10. Return authenticated OAuth2 principal
        return new DefaultOAuth2User(
                authorities,
                attributes,
                "id"
        );
    }
}
