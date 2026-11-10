
package kh.edu.istad.platform.restapi.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        List<String> authorities = new ArrayList<>();

        // 1. Read OAuth2 scopes
        List<String> scopes = jwt.getClaimAsStringList("scope");

        if (scopes != null) {
            scopes.forEach(scope ->
                    authorities.add("SCOPE_" + scope)
            );
        }

        // 2. Read user permissions
        List<String> permissions =
                jwt.getClaimAsStringList("permissions");

        if (permissions == null) {
            permissions = Collections.emptyList();
        }

        authorities.addAll(permissions);

        // 3. Read user roles
        List<String> roles =
                jwt.getClaimAsStringList("roles");

        if (roles != null) {
            authorities.addAll(roles);
        }

        // 4. Convert to Spring Security authorities
        var grantedAuthorities = authorities.stream()
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new JwtAuthenticationToken(
                jwt,
                grantedAuthorities
        );
    }
}
