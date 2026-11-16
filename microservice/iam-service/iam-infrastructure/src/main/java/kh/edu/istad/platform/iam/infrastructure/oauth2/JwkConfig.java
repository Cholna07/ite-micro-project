package kh.edu.istad.platform.iam.infrastructure.oauth2;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import kh.edu.istad.platform.iam.infrastructure.jwt.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.security.cert.Certificate;

@Configuration
public class JwkConfig {

    @Bean
    public RSAKey iamSigningJwk(JwtProperties properties) {
        String location = properties.getKeyStorePath();
        String storePassword = properties.getKeyStorePassword();
        String alias = properties.getKeyAlias();
        String keyPassword = properties.getKeyPassword();

        if (isBlank(location) || isBlank(storePassword) || isBlank(alias)) {
            throw configurationError("Set IAM_JWT_KEYSTORE_PATH, IAM_JWT_KEYSTORE_PASSWORD, and IAM_JWT_KEY_ALIAS.");
        }

        if (isBlank(keyPassword)) {
            keyPassword = storePassword;
        }

        try (InputStream input = Files.newInputStream(Path.of(location))) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(input, storePassword.toCharArray());

            if (!keyStore.containsAlias(alias) || !keyStore.isKeyEntry(alias)) {
                throw configurationError("The configured PKCS12 alias is missing or is not a private-key entry.");
            }

            Key key = keyStore.getKey(alias, keyPassword.toCharArray());
            Certificate certificate = keyStore.getCertificate(alias);
            if (!(key instanceof RSAPrivateKey privateKey)
                    || certificate == null
                    || !(certificate.getPublicKey() instanceof RSAPublicKey publicKey)) {
                throw configurationError("The configured alias must contain an RSA private key and RSA certificate.");
            }

            verifyKeyPair(privateKey, publicKey);
            String keyId = "sha256-" + Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(MessageDigest.getInstance("SHA-256").digest(publicKey.getEncoded()));

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(keyId)
                    .build();
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to load the IAM JWT PKCS12 signing key. Check the configured path, passwords, and alias; no replacement key was generated.",
                    exception
            );
        }
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey iamSigningJwk) {
        JWKSet jwkSet = new JWKSet(iamSigningJwk);
        return (selector, context) -> selector.select(jwkSet);
    }

    private static void verifyKeyPair(RSAPrivateKey privateKey, RSAPublicKey publicKey) throws Exception {
        byte[] challenge = "IAM JWT signing key validation".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(challenge);
        byte[] signature = signer.sign();

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(challenge);
        if (!verifier.verify(signature)) {
            throw configurationError("The private key and certificate public key do not match.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static IllegalStateException configurationError(String message) {
        return new IllegalStateException("Invalid IAM JWT signing key configuration. " + message);
    }
}
