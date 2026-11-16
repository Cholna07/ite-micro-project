package kh.edu.istad.platform.iam.infrastructure.jwt;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.proc.SecurityContext;
import kh.edu.istad.platform.iam.domain.port.out.UserSessionRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.infrastructure.oauth2.JwkConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PersistentJwtSigningTest {
    private static final String STORE_PASSWORD = "test-only-password";
    private static final String KEY_ALIAS = "iam-test-key";

    @TempDir
    Path temporaryDirectory;

    @Test
    void signsAndValidatesRs256PreservingClaimsAndSessionId() throws Exception {
        JwtProperties properties = properties(createTestKeystore("jwt-claims.p12"));
        JwtSigning signing = loadSigning(properties);
        JwtTokenService tokenService = new JwtTokenService(signing.encoder(), properties);
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(Duration.ofMinutes(15));

        String compact = tokenService.generateAccessToken(userId, "sovann", List.of("user.read", "role.read"),
                sessionId, issuedAt, expiresAt);
        Jwt jwt = signing.decoder().decode(compact);

        assertEquals("RS256", jwt.getHeaders().get("alg"));
        assertEquals("cholna-iam", jwt.getClaimAsString("iss"));
        assertEquals(userId.toString(), jwt.getSubject());
        assertEquals("sovann", jwt.getClaimAsString("username"));
        assertEquals(List.of("user.read", "role.read"), jwt.getClaimAsStringList("permissions"));
        assertEquals("IAM_SESSION", jwt.getClaimAsString("iam_token_type"));
        assertEquals(sessionId.toString(), jwt.getClaimAsString("iam_session_id"));
        assertEquals(issuedAt.truncatedTo(java.time.temporal.ChronoUnit.SECONDS), jwt.getIssuedAt());
        assertEquals(expiresAt.truncatedTo(java.time.temporal.ChronoUnit.SECONDS), jwt.getExpiresAt());
        verify(signing.sessions()).isActive(eq(sessionId), eq(userId), any());
        verify(signing.sessions()).touchActivity(eq(sessionId), eq(userId), any());
    }

    @Test
    void decoderRejectsExpiredToken() throws Exception {
        JwtSigning signing = loadSigning(properties(createTestKeystore("jwt-expired.p12")));
        Instant expired = Instant.now().minusSeconds(300);
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("cholna-iam").subject("expired-user")
                .issuedAt(expired.minusSeconds(60)).expiresAt(expired).build();
        String compact = encode(signing.encoder(), claims);

        assertThrows(JwtException.class, () -> signing.decoder().decode(compact));
    }

    @Test
    void decoderRejectsInvalidSignature() throws Exception {
        JwtSigning signer = loadSigning(properties(createTestKeystore("jwt-signature-a.p12")));
        JwtSigning verifier = loadSigning(properties(createTestKeystore("jwt-signature-b.p12")));
        UUID userId = UUID.randomUUID();
        String compact = customToken(signer.encoder(), userId, UUID.randomUUID());

        assertThrows(JwtException.class, () -> verifier.decoder().decode(compact));
    }

    @Test
    void customIamTokenWithoutMarkerIsRejected() throws Exception {
        JwtSigning signing = loadSigning(properties(createTestKeystore("jwt-missing-session.p12")));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("cholna-iam").subject(UUID.randomUUID().toString())
                .issuedAt(now).expiresAt(now.plusSeconds(300)).claim("username", "sovann")
                .claim("permissions", List.of("user.read")).build();

        assertThrows(JwtException.class, () -> signing.decoder().decode(encode(signing.encoder(), claims)));
    }

    @Test
    void decoderRejectsRevokedMissingExpiredOrMismatchedSessionRecord() throws Exception {
        JwtSigning signing = loadSigning(properties(createTestKeystore("jwt-inactive-session.p12")));
        UUID userUuid = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String compact = customToken(signing.encoder(), userUuid, sessionId);
        when(signing.sessions().isActive(eq(sessionId), eq(userUuid), any())).thenReturn(false);

        assertThrows(JwtException.class, () -> signing.decoder().decode(compact));
    }

    @Test
    void samePersistentKeyValidatesSessionTokenAfterReconstruction() throws Exception {
        Path keystore = createTestKeystore("jwt-restart.p12");
        JwtSigning beforeRestart = loadSigning(properties(keystore));
        UUID userUuid = UUID.randomUUID();
        String tokenBeforeRestart = customToken(beforeRestart.encoder(), userUuid, UUID.randomUUID());
        JwtSigning afterRestart = loadSigning(properties(keystore));

        assertEquals(beforeRestart.jwk().getKeyID(), afterRestart.jwk().getKeyID());
        assertEquals(beforeRestart.jwk().toPublicJWK().toJSONString(), afterRestart.jwk().toPublicJWK().toJSONString());
        assertEquals(userUuid.toString(), afterRestart.decoder().decode(tokenBeforeRestart).getSubject());
    }

    @Test
    void sharedJwkSourcePreservesAuthorizationServerAndOidcJwtValidation() throws Exception {
        JwtSigning signing = loadSigning(properties(createTestKeystore("jwt-oauth-shared.p12")));
        List<JWK> selected = signing.jwkSource().get(new JWKSelector(new JWKMatcher.Builder().build()), null);
        assertEquals(1, selected.size());
        assertEquals(signing.jwk().getKeyID(), selected.getFirst().getKeyID());
        assertTrue(((RSAKey) selected.getFirst()).isPrivate());
        assertFalse(((RSAKey) selected.getFirst()).toPublicJWK().isPrivate());

        Instant now = Instant.now();
        JwtClaimsSet oauthClaims = JwtClaimsSet.builder().issuer("authorization-server").subject("client-or-user")
                .issuedAt(now).expiresAt(now.plusSeconds(300)).claim("permissions", List.of("api.read")).build();
        String compact = encode(signing.encoder(), oauthClaims);
        assertEquals("client-or-user", signing.decoder().decode(compact).getSubject());
        JwtDecoder authorizationServerDecoder = OAuth2AuthorizationServerConfiguration.jwtDecoder(signing.jwkSource());
        assertEquals("client-or-user", authorizationServerDecoder.decode(compact).getSubject());
        verifyNoInteractions(signing.sessions());
    }

    private String customToken(JwtEncoder encoder, UUID userUuid, UUID sessionId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("cholna-iam").subject(userUuid.toString())
                .issuedAt(now).expiresAt(now.plusSeconds(300)).claim("username", "sovann")
                .claim("permissions", List.of("user.read")).claim("iam_token_type", "IAM_SESSION")
                .claim("iam_session_id", sessionId.toString()).build();
        return encode(encoder, claims);
    }

    private String encode(JwtEncoder encoder, JwtClaimsSet claims) {
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
                .getTokenValue();
    }

    private Path createTestKeystore(String filename) throws Exception {
        Path path = temporaryDirectory.resolve(filename);
        String executable = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").toLowerCase().contains("win") ? "keytool.exe" : "keytool").toString();
        Process process = new ProcessBuilder(executable, "-genkeypair", "-alias", KEY_ALIAS,
                "-storetype", "PKCS12", "-keystore", path.toString(), "-storepass", STORE_PASSWORD,
                "-keypass", STORE_PASSWORD, "-dname", "CN=IAM Test Key", "-keyalg", "RSA", "-keysize", "2048",
                "-validity", "3650", "-noprompt").redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes());
        if (process.waitFor() != 0) fail("Could not generate test-only PKCS12 key with keytool: " + output);
        return path;
    }

    private JwtProperties properties(Path path) {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("cholna-iam");
        properties.setAccessTokenExpiration(Duration.ofMinutes(15));
        properties.setKeyStorePath(path.toString());
        properties.setKeyStorePassword(STORE_PASSWORD);
        properties.setKeyAlias(KEY_ALIAS);
        properties.setKeyPassword(STORE_PASSWORD);
        return properties;
    }

    private JwtSigning loadSigning(JwtProperties properties) throws Exception {
        JwkConfig config = new JwkConfig();
        RSAKey key = config.iamSigningJwk(properties);
        var source = config.jwkSource(key);
        JwtConfig jwtConfig = new JwtConfig();
        UserSessionRepository sessions = mock(UserSessionRepository.class);
        when(sessions.isActive(any(), any(), any())).thenReturn(true);
        when(sessions.touchActivity(any(), any(), any())).thenReturn(true);
        UserRepository users = mock(UserRepository.class);
        when(users.findByUuid(any())).thenAnswer(invocation -> {
            User user = new User();
            user.setUuid(invocation.getArgument(0));
            user.setEnabled(true);
            user.setAccountNonLocked(true);
            return java.util.Optional.of(user);
        });
        return new JwtSigning(key, source, jwtConfig.jwtEncoder(source),
                jwtConfig.jwtDecoder(key, sessions, users), sessions);
    }

    private record JwtSigning(RSAKey jwk,
                              com.nimbusds.jose.jwk.source.JWKSource<SecurityContext> jwkSource,
                              JwtEncoder encoder, JwtDecoder decoder, UserSessionRepository sessions) {
    }
}
