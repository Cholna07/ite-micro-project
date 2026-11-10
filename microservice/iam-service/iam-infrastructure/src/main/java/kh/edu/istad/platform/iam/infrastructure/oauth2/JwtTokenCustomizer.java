package kh.edu.istad.platform.iam.infrastructure.oauth2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class JwtTokenCustomizer {

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {

            if (context.getPrincipal() == null) {
                return;
            }

            if (context.getTokenType().getValue().equals("access_token")) {

                context.getClaims().claim(
                        "roles",
                        context.getPrincipal().getAuthorities()
                                .stream()
                                .map(authority -> authority.getAuthority())
                                .filter(authority -> authority.startsWith("ROLE_"))
                                .toList()
                );

                context.getClaims().claim(
                        "permissions",
                        context.getPrincipal().getAuthorities()
                                .stream()
                                .map(authority -> authority.getAuthority())
                                .filter(authority -> !authority.startsWith("ROLE_"))
                                .toList()
                );
            }
        };
    }
}
