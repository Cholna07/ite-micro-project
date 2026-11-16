package kh.edu.istad.platform.iam.infrastructure.social;

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

    public SocialLoginConfig(FederatedOidcUserService federatedOidcUserService,
                             FederatedOAuth2UserService federatedOAuth2UserService) {
        this.federatedOidcUserService = federatedOidcUserService;
        this.federatedOAuth2UserService = federatedOAuth2UserService;
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain socialLoginSecurityFilterChain(
            HttpSecurity http,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository
    ) throws Exception {
        http.securityMatcher("/oauth2/authorization/**", "/login/oauth2/code/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint.authorizationRequestRepository(authorizationRequestRepository))
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(federatedOidcUserService)
                                .userService(federatedOAuth2UserService)));
        return http.build();
    }
}
