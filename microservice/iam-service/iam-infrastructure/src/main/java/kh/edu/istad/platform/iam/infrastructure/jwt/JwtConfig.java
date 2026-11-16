package kh.edu.istad.platform.iam.infrastructure.jwt;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.JOSEException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public JwtEncoder jwtEncoder(
            JWKSource<SecurityContext> jwkSource
    ) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(
            RSAKey iamSigningJwk,
            UserSessionRepository userSessionRepository,
            UserRepository userRepository
    ) throws JOSEException {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(iamSigningJwk.toRSAPublicKey())
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        OAuth2TokenValidator<org.springframework.security.oauth2.jwt.Jwt> defaults = JwtValidators.createDefault();
        IamSessionJwtValidator sessionValidator = new IamSessionJwtValidator(userSessionRepository, userRepository);
        decoder.setJwtValidator(jwt -> {
            var timestampResult = defaults.validate(jwt);
            if (timestampResult.hasErrors()) return timestampResult;
            return sessionValidator.validate(jwt);
        });
        return decoder;
    }
}
