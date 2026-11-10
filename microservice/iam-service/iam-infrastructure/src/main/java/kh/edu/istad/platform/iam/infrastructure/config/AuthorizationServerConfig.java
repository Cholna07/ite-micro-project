
package kh.edu.istad.platform.iam.infrastructure.config;

import kh.edu.istad.platform.iam.infrastructure.oauth2.OAuth2AuthorizationConfig;
import kh.edu.istad.platform.iam.infrastructure.oauth2.OAuth2ConsentConfig;
import kh.edu.istad.platform.iam.infrastructure.oauth2.OidcInfoService;
import kh.edu.istad.platform.iam.infrastructure.oauth2.RegisteredClientConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@Import({
        RegisteredClientConfig.class,
        OAuth2AuthorizationConfig.class,
        OAuth2ConsentConfig.class
})
public class AuthorizationServerConfig {

    private final OidcInfoService oidcInfoService;

    public AuthorizationServerConfig(OidcInfoService oidcInfoService) {
        this.oidcInfoService = oidcInfoService;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, authorizationServer ->
                        authorizationServer
                                .oidc(oidc -> oidc
                                        .userInfoEndpoint(userInfo ->
                                                userInfo.userInfoMapper(oidcInfoService)
                                        )
                                )
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .exceptionHandling(exceptions ->
                        exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );

        return http.build();
    }
}
