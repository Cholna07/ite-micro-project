package kh.edu.istad.platform.iam.infrastructure.config;

import kh.edu.istad.platform.iam.infrastructure.oauth2.OAuth2AuthorizationConfig;
import kh.edu.istad.platform.iam.infrastructure.oauth2.OAuth2ConsentConfig;
import kh.edu.istad.platform.iam.infrastructure.oauth2.OidcInfoService;
import kh.edu.istad.platform.iam.infrastructure.oauth2.RegisteredClientConfig;
import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
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
    private final AuthenticationProvider authenticationProvider;

    public AuthorizationServerConfig(
            OidcInfoService oidcInfoService,
            AuthenticationProvider authenticationProvider
    ) {
        this.oidcInfoService = oidcInfoService;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            CookieCsrfTokenRepository csrfTokenRepository,
            @Qualifier("spaCsrfCookieFilter") Filter spaCsrfCookieFilter,
            @Qualifier("sessionRevocationFilter") Filter sessionRevocationFilter
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
                                .authorizationEndpoint(endpoint -> endpoint.consentPage("/oauth2/consent"))
                )
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .formLogin(form -> form.permitAll())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .exceptionHandling(exceptions ->
                        exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login?flow=oauth"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );

        http.addFilterAfter(spaCsrfCookieFilter, CsrfFilter.class);
        http.addFilterAfter(sessionRevocationFilter, SecurityContextHolderFilter.class);
        return http.build();
    }
}
