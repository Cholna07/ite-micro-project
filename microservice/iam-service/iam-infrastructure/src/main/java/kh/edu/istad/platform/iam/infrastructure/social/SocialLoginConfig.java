package kh.edu.istad.platform.iam.infrastructure.social;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SocialLoginConfig {

    private final FederatedOidcUserService federatedOidcUserService;
    private final FederatedOAuth2UserService federatedOAuth2UserService;

    public SocialLoginConfig(
            FederatedOidcUserService federatedOidcUserService,
            FederatedOAuth2UserService federatedOAuth2UserService
    ) {
        this.federatedOidcUserService = federatedOidcUserService;
        this.federatedOAuth2UserService = federatedOAuth2UserService;
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest>
    authorizationRequestRepository() {

        HttpSessionOAuth2AuthorizationRequestRepository delegate =
                new HttpSessionOAuth2AuthorizationRequestRepository();

        return new AuthorizationRequestRepository<>() {

            @Override
            public OAuth2AuthorizationRequest loadAuthorizationRequest(
                    HttpServletRequest request
            ) {
                OAuth2AuthorizationRequest authorizationRequest =
                        delegate.loadAuthorizationRequest(request);

                System.out.println(
                        "OAuth2 callback - saved authorization request exists: "
                                + (authorizationRequest != null)
                );

                return authorizationRequest;
            }

            @Override
            public void saveAuthorizationRequest(
                    OAuth2AuthorizationRequest authorizationRequest,
                    HttpServletRequest request,
                    HttpServletResponse response
            ) {
                delegate.saveAuthorizationRequest(
                        authorizationRequest,
                        request,
                        response
                );
            }

            @Override
            public OAuth2AuthorizationRequest removeAuthorizationRequest(
                    HttpServletRequest request,
                    HttpServletResponse response
            ) {
                OAuth2AuthorizationRequest authorizationRequest =
                        delegate.loadAuthorizationRequest(request);

                String callbackState = request.getParameter("state");
                String authorizationCode = request.getParameter("code");
                String providerError = request.getParameter("error");

                System.out.println(
                        "===== SOCIAL LOGIN CALLBACK DEBUG ====="
                );

                System.out.println(
                        "Saved request exists: "
                                + (authorizationRequest != null)
                );

                System.out.println(
                        "Callback state exists: "
                                + (callbackState != null)
                );

                System.out.println(
                        "Authorization code exists: "
                                + (authorizationCode != null)
                );

                System.out.println(
                        "Provider error: " + providerError
                );

                if (authorizationRequest != null
                        && callbackState != null) {

                    System.out.println(
                            "State matches: "
                                    + authorizationRequest.getState()
                                    .equals(callbackState)
                    );
                }

                return delegate.removeAuthorizationRequest(
                        request,
                        response
                );
            }
        };
    }

    @Bean
    @Order(2)
    public SecurityFilterChain socialLoginSecurityFilterChain(
            HttpSecurity http,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest>
                    authorizationRequestRepository
    ) throws Exception {

        http
                .securityMatcher(
                        "/oauth2/authorization/**",
                        "/login/oauth2/code/**"
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestRepository(
                                        authorizationRequestRepository
                                )
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(
                                        federatedOidcUserService
                                )
                                .userService(
                                        federatedOAuth2UserService
                                )
                        )
                );

        return http.build();
    }
}
