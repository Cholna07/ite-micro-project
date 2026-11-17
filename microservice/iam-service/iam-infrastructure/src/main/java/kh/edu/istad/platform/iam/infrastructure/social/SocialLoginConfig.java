package kh.edu.istad.platform.iam.infrastructure.social;

import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordLoginEventUseCase;
import kh.edu.istad.platform.iam.domain.dto.command.BrowserSessionContext;
import kh.edu.istad.platform.iam.domain.usecase.user.HttpSessionInventory;
import kh.edu.istad.platform.iam.domain.usecase.user.RegisterBrowserSessionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.identity.IdentityLinkSession;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Qualifier;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.util.UUID;

@Configuration
public class SocialLoginConfig {
    private static final Logger log = LoggerFactory.getLogger(SocialLoginConfig.class);
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
    public OAuth2AuthorizationRequestResolver socialAuthorizationRequestResolver(
            ClientRegistrationRepository registrations
    ) {
        return new GitHubPkceAuthorizationRequestResolver(registrations);
    }

    @Bean
    @Order(2)
    public SecurityFilterChain socialLoginSecurityFilterChain(
            HttpSecurity http,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository,
            OAuth2AuthorizationRequestResolver socialAuthorizationRequestResolver,
            RecordLoginEventUseCase recordLoginEventUseCase,
            RegisterBrowserSessionUseCase registerBrowserSession,
            @Qualifier("sessionRevocationFilter") Filter sessionRevocationFilter
    ) throws Exception {
        http.securityMatcher("/oauth2/authorization/**", "/login/oauth2/code/**")
                // Initiation and callback are public protocol endpoints. OAuth2 state,
                // redirect URI validation, and the session-backed authorization request
                // repository protect the callback; the provider must be reachable before sign-in.
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestRepository(authorizationRequestRepository)
                                .authorizationRequestResolver(socialAuthorizationRequestResolver))
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(federatedOidcUserService)
                                .userService(federatedOAuth2UserService))
                        .successHandler((request, response, authentication) -> {
                            var session = request.getSession(true);
                            UUID linkingUserUuid = IdentityLinkSession.parseUserUuid(
                                    session.getAttribute(IdentityLinkSession.USER_UUID_ATTRIBUTE));
                            if (linkingUserUuid != null) {
                                session.removeAttribute(IdentityLinkSession.USER_UUID_ATTRIBUTE);
                                try {
                                    if (session.getAttribute(HttpSessionInventory.SESSION_RECORD_ID) == null) {
                                        String provider = providerFromCallback(request.getRequestURI());
                                        var id = registerBrowserSession.execute(authentication.getName(),
                                                new BrowserSessionContext(request.getRemoteAddr(), request.getHeader("User-Agent"),
                                                        "SOCIAL", provider, session.getMaxInactiveInterval()));
                                        session.setAttribute(HttpSessionInventory.SESSION_RECORD_ID, id.toString());
                                    }
                                    response.sendRedirect("/account/linked?link=success");
                                } catch (RuntimeException exception) {
                                    org.springframework.security.core.context.SecurityContextHolder.clearContext();
                                    try { session.invalidate(); } catch (IllegalStateException ignored) { }
                                    response.sendError(503, "Unable to establish an authenticated session");
                                }
                                return;
                            }
                            recordSocialLoginSafely(recordLoginEventUseCase, authentication.getName(),
                                    request, LoginEventOutcome.SUCCESS, null);
                            try {
                                String provider = providerFromCallback(request.getRequestURI());
                                var id = registerBrowserSession.execute(authentication.getName(),
                                        new BrowserSessionContext(request.getRemoteAddr(), request.getHeader("User-Agent"),
                                                "SOCIAL", provider, session.getMaxInactiveInterval()));
                                session.setAttribute(HttpSessionInventory.SESSION_RECORD_ID, id.toString());
                                new SavedRequestAwareAuthenticationSuccessHandler()
                                        .onAuthenticationSuccess(request, response, authentication);
                            } catch (RuntimeException exception) {
                                org.springframework.security.core.context.SecurityContextHolder.clearContext();
                                var failedSession = request.getSession(false);
                                if (failedSession != null) try { failedSession.invalidate(); } catch (IllegalStateException ignored) { }
                                response.sendError(503, "Unable to establish an authenticated session");
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            var session = request.getSession(false);
                            if (session != null && IdentityLinkSession.parseUserUuid(
                                    session.getAttribute(IdentityLinkSession.USER_UUID_ATTRIBUTE)) != null) {
                                log.warn("Social account linking failed for provider {}", providerFromCallback(request.getRequestURI()), exception);
                                session.removeAttribute(IdentityLinkSession.USER_UUID_ATTRIBUTE);
                                response.sendRedirect("/account/linked?link=failed");
                                return;
                            }
                            recordSocialLoginSafely(recordLoginEventUseCase, null,
                                    request, LoginEventOutcome.FAILURE, "SOCIAL_LOGIN_FAILED");
                            new SimpleUrlAuthenticationFailureHandler("/login?error")
                                    .onAuthenticationFailure(request, response, exception);
                        }));
        http.addFilterAfter(sessionRevocationFilter, SecurityContextHolderFilter.class);
        return http.build();
    }

    private static void recordSocialLoginSafely(RecordLoginEventUseCase recorder, String username,
                                                HttpServletRequest request, LoginEventOutcome outcome,
                                                String failureCategory) {
        try {
            recorder.execute(username, outcome, request.getRemoteAddr(), request.getHeader("User-Agent"), failureCategory);
        } catch (RuntimeException exception) {
            log.error("Could not persist social login event with outcome {}", outcome, exception);
        }
    }

    private static String providerFromCallback(String uri) {
        String prefix = "/login/oauth2/code/";
        if (uri == null) return null;
        int index = uri.indexOf(prefix);
        if (index < 0) return null;
        String provider = uri.substring(index + prefix.length());
        int slash = provider.indexOf('/');
        return slash < 0 ? provider : provider.substring(0, slash);
    }
}
