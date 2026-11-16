package kh.edu.istad.platform.iam.infrastructure.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "iam.security.jwt")
public class JwtProperties {

    private String issuer = "cholna-iam";

    private String secret;

    private String keyStorePath;

    private String keyStorePassword;

    private String keyAlias;

    private String keyPassword;

    private Duration accessTokenExpiration = Duration.ofMinutes(15);

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getKeyStorePath() { return keyStorePath; }

    public void setKeyStorePath(String keyStorePath) { this.keyStorePath = keyStorePath; }

    public String getKeyStorePassword() { return keyStorePassword; }

    public void setKeyStorePassword(String keyStorePassword) { this.keyStorePassword = keyStorePassword; }

    public String getKeyAlias() { return keyAlias; }

    public void setKeyAlias(String keyAlias) { this.keyAlias = keyAlias; }

    public String getKeyPassword() { return keyPassword; }

    public void setKeyPassword(String keyPassword) { this.keyPassword = keyPassword; }

    public Duration getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(Duration accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }
}
