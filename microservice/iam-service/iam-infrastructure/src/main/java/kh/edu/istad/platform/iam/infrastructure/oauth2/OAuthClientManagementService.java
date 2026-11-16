package kh.edu.istad.platform.iam.infrastructure.oauth2;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class OAuthClientManagementService {
    private static final Pattern SCOPE_PATTERN = Pattern.compile("[A-Za-z0-9._:-]{1,100}");
    private static final Set<String> OIDC_SCOPES = Set.of("openid", "profile", "email", "client.create");
    private static final Set<String> GRANT_TYPES = Set.of("authorization_code", "refresh_token", "client_credentials");
    private static final Set<String> AUTH_METHODS = Set.of("client_secret_basic", "client_secret_post");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate jdbc;
    private final RegisteredClientRepository clientStorage;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissions;

    public OAuthClientManagementService(
            JdbcTemplate jdbc,
            @Qualifier("oauth2RegisteredClientStorage") RegisteredClientRepository clientStorage,
            PasswordEncoder passwordEncoder,
            PermissionRepository permissions
    ) {
        this.jdbc = jdbc;
        this.clientStorage = clientStorage;
        this.passwordEncoder = passwordEncoder;
        this.permissions = permissions;
    }

    @Transactional(readOnly = true)
    public ClientPage list(String keyword, int page, int size, String sortBy, String sortDirection) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        Map<String, String> sortColumns = Map.of("clientId", "client_id", "clientName", "client_name", "createdAt", "client_id_issued_at");
        String column = sortBy == null ? null : sortColumns.get(sortBy);
        if (column == null) throw new IllegalArgumentException("Unsupported client sort field");
        if (sortDirection == null || !(sortDirection.equalsIgnoreCase("ASC") || sortDirection.equalsIgnoreCase("DESC")))
            throw new IllegalArgumentException("Sort direction must be ASC or DESC");

        String filter = keyword == null || keyword.isBlank() ? null : keyword.trim().toLowerCase(Locale.ROOT);
        String where = filter == null ? "" : " WHERE POSITION(? IN LOWER(client_id)) > 0 OR POSITION(? IN LOWER(client_name)) > 0";
        Long total = filter == null
                ? jdbc.queryForObject("SELECT COUNT(*) FROM oauth2_registered_client", Long.class)
                : jdbc.queryForObject("SELECT COUNT(*) FROM oauth2_registered_client" + where, Long.class, filter, filter);
        long totalElements = total == null ? 0 : total;
        String sql = "SELECT client_id FROM oauth2_registered_client" + where + " ORDER BY " + column + " " + sortDirection.toUpperCase(Locale.ROOT)
                + " LIMIT ? OFFSET ?";
        List<Object> parameters = new ArrayList<>();
        if (filter != null) { parameters.add(filter); parameters.add(filter); }
        parameters.add(size);
        parameters.add((long) page * size);
        List<String> ids = jdbc.query(sql, (rs, row) -> rs.getString("client_id"), parameters.toArray());
        List<ClientDetails> content = ids.stream().map(clientStorage::findByClientId)
                .filter(client -> client != null).map(this::toDetails).toList();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new ClientPage(content, new ClientPageMetadata(page, size, totalElements, totalPages, page == 0, page >= totalPages - 1));
    }

    @Transactional(readOnly = true)
    public ClientDetails get(String clientId) {
        return toDetails(requireClient(clientId));
    }

    @Transactional
    public CreatedClient create(ClientConfiguration request) {
        validate(request, null);
        String clientId = UUID.randomUUID().toString();
        String rawSecret = generateSecret();
        RegisteredClient client = buildClient(RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId).clientIdIssuedAt(java.time.Instant.now())
                .clientSecret(passwordEncoder.encode(rawSecret)).clientName(request.clientName().trim()), request, null, false);
        save(client);
        return new CreatedClient(toDetails(client), rawSecret);
    }

    @Transactional
    public ClientDetails update(String clientId, ClientConfiguration request) {
        RegisteredClient existing = requireClient(clientId);
        validate(request, existing);
        RegisteredClient updated = buildClient(RegisteredClient.from(existing), request, existing, isDisabled(existing));
        save(updated);
        return toDetails(updated);
    }

    @Transactional
    public void deactivate(String clientId) {
        RegisteredClient existing = requireClient(clientId);
        setDisabled(existing, true);
    }

    @Transactional
    public ClientDetails activate(String clientId) {
        RegisteredClient existing = requireClient(clientId);
        RegisteredClient updated = withDisabled(existing, false);
        save(updated);
        return toDetails(updated);
    }

    @Transactional
    public CreatedClient rotateSecret(String clientId) {
        RegisteredClient existing = requireClient(clientId);
        if (isDisabled(existing)) throw new IllegalStateException("A disabled client cannot rotate its secret");
        String rawSecret = generateSecret();
        RegisteredClient updated = RegisteredClient.from(existing)
                .clientSecret(passwordEncoder.encode(rawSecret))
                .clientSecretExpiresAt(null)
                .build();
        save(updated);
        return new CreatedClient(toDetails(updated), rawSecret);
    }

    private RegisteredClient requireClient(String clientId) {
        if (clientId == null || clientId.isBlank()) throw new IllegalArgumentException("Client ID is required");
        RegisteredClient client = clientStorage.findByClientId(clientId);
        if (client == null) throw new ClientNotFoundException();
        return client;
    }

    private void save(RegisteredClient client) {
        try {
            clientStorage.save(client);
        } catch (DataIntegrityViolationException exception) {
            throw new ClientConflictException();
        }
    }

    private RegisteredClient buildClient(RegisteredClient.Builder builder, ClientConfiguration request,
                                          RegisteredClient existing, boolean disabled) {
        builder.clientName(request.clientName().trim());
        builder.clientAuthenticationMethods(methods -> {
            methods.clear();
            request.clientAuthenticationMethods().forEach(value -> methods.add(authenticationMethod(value)));
        });
        builder.authorizationGrantTypes(grants -> {
            grants.clear();
            request.authorizationGrantTypes().forEach(value -> grants.add(grantType(value)));
        });
        builder.redirectUris(uris -> { uris.clear(); uris.addAll(request.redirectUris()); });
        builder.postLogoutRedirectUris(uris -> { uris.clear(); uris.addAll(request.postLogoutRedirectUris()); });
        builder.scopes(scopes -> { scopes.clear(); scopes.addAll(request.scopes()); });

        Map<String, Object> clientSettings = existing == null
                ? new HashMap<>() : new HashMap<>(existing.getClientSettings().getSettings());
        clientSettings.put("requireAuthorizationConsent", request.requireAuthorizationConsent());
        clientSettings.put("requireProofKey", request.requireProofKey());
        clientSettings.put(ActiveRegisteredClientRepository.DISABLED_SETTING, disabled);
        builder.clientSettings(ClientSettings.withSettings(clientSettings).build());

        Map<String, Object> tokenSettings = existing == null
                ? new HashMap<>(TokenSettings.builder().build().getSettings())
                : new HashMap<>(existing.getTokenSettings().getSettings());
        builder.tokenSettings(TokenSettings.builder().settings(settings -> settings.putAll(tokenSettings))
                .accessTokenTimeToLive(Duration.ofSeconds(request.accessTokenTtlSeconds()))
                .refreshTokenTimeToLive(Duration.ofSeconds(request.refreshTokenTtlSeconds()))
                .reuseRefreshTokens(request.reuseRefreshTokens())
                .build());
        return builder.build();
    }

    private RegisteredClient withDisabled(RegisteredClient client, boolean disabled) {
        Map<String, Object> settings = new HashMap<>(client.getClientSettings().getSettings());
        settings.put(ActiveRegisteredClientRepository.DISABLED_SETTING, disabled);
        return RegisteredClient.from(client).clientSettings(ClientSettings.withSettings(settings).build()).build();
    }

    private void setDisabled(RegisteredClient client, boolean disabled) {
        save(withDisabled(client, disabled));
    }

    private void validate(ClientConfiguration request, RegisteredClient existing) {
        if (request == null || request.clientName() == null || request.clientName().isBlank() || request.clientName().trim().length() > 200)
            throw new IllegalArgumentException("Client name is required and must be at most 200 characters");
        requireSet(request.clientAuthenticationMethods(), "Client authentication methods");
        requireSet(request.authorizationGrantTypes(), "Authorization grant types");
        requireSet(request.scopes(), "Scopes");
        requireSetPresent(request.redirectUris(), "Redirect URIs");
        requireSetPresent(request.postLogoutRedirectUris(), "Post-logout redirect URIs");
        if (request.clientAuthenticationMethods().stream().anyMatch(value -> !AUTH_METHODS.contains(value)))
            throw new IllegalArgumentException("Only client_secret_basic and client_secret_post are supported");
        if (request.authorizationGrantTypes().stream().anyMatch(value -> !GRANT_TYPES.contains(value)))
            throw new IllegalArgumentException("Unsupported authorization grant type");
        if (request.authorizationGrantTypes().contains("refresh_token") && !request.authorizationGrantTypes().contains("authorization_code"))
            throw new IllegalArgumentException("refresh_token requires authorization_code");
        if (request.authorizationGrantTypes().contains("client_credentials")
                && request.authorizationGrantTypes().size() != 1)
            throw new IllegalArgumentException("client_credentials must be configured as a standalone grant");
        boolean authorizationCode = request.authorizationGrantTypes().contains("authorization_code");
        if (request.scopes().contains("openid") && !authorizationCode)
            throw new IllegalArgumentException("The openid scope requires authorization_code");
        if (authorizationCode && request.redirectUris().isEmpty())
            throw new IllegalArgumentException("Authorization code clients require at least one redirect URI");
        if (!authorizationCode && !request.redirectUris().isEmpty())
            throw new IllegalArgumentException("Redirect URIs are only supported for authorization_code clients");
        if (request.requireProofKey() && !authorizationCode)
            throw new IllegalArgumentException("PKCE can only be required for authorization_code clients");
        if (!request.postLogoutRedirectUris().isEmpty()
                && !(authorizationCode && request.scopes().contains("openid")))
            throw new IllegalArgumentException("Post-logout redirect URIs require an OpenID Connect authorization_code client");
        if (request.scopes().stream().anyMatch(scope -> !SCOPE_PATTERN.matcher(scope).matches()))
            throw new IllegalArgumentException("Scopes must be valid tokens of at most 100 characters");
        Set<String> allowedScopes = new LinkedHashSet<>(OIDC_SCOPES);
        permissions.findAll().stream().filter(Permission::isActive).filter(permission -> !permission.isDeleted())
                .map(permission -> permission.getName().value()).forEach(allowedScopes::add);
        if (existing != null) allowedScopes.addAll(existing.getScopes());
        if (!allowedScopes.containsAll(request.scopes())) throw new IllegalArgumentException("One or more scopes are not supported by this IAM server");
        if (request.accessTokenTtlSeconds() < 60 || request.accessTokenTtlSeconds() > 3600)
            throw new IllegalArgumentException("Access token lifetime must be between 60 and 3600 seconds");
        if (request.refreshTokenTtlSeconds() < 300 || request.refreshTokenTtlSeconds() > 31_536_000)
            throw new IllegalArgumentException("Refresh token lifetime must be between 300 seconds and 365 days");
        if (request.clientAuthenticationMethods().size() > 2 || request.authorizationGrantTypes().size() > 3)
            throw new IllegalArgumentException("Too many client authentication methods or grant types");
        validateUris(request.redirectUris(), "redirect URI");
        validateUris(request.postLogoutRedirectUris(), "post-logout redirect URI");
        if (joinedLength(request.redirectUris()) > 1000 || joinedLength(request.postLogoutRedirectUris()) > 1000)
            throw new IllegalArgumentException("The combined redirect URI values exceed the supported storage length");
        if (joinedLength(request.scopes()) > 1000) throw new IllegalArgumentException("The combined scopes exceed the supported storage length");
    }

    private void validateUris(Set<String> values, String label) {
        for (String value : values) {
            if (value == null || value.isBlank() || value.length() > 2000)
                throw new IllegalArgumentException("Each " + label + " must be nonblank and at most 2000 characters");
            try {
                URI uri = URI.create(value.trim());
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
                boolean secureWeb = scheme.equals("https") && uri.getHost() != null;
                boolean loopbackHttp = scheme.equals("http") && uri.getHost() != null && isLoopback(uri.getHost());
                if ((!secureWeb && !loopbackHttp) || uri.getUserInfo() != null || uri.getFragment() != null)
                    throw new IllegalArgumentException("Only HTTPS URLs or HTTP loopback URLs without user-info or fragments are allowed for " + label);
            } catch (IllegalArgumentException exception) {
                if (exception.getMessage() != null && exception.getMessage().startsWith("Only HTTPS")) throw exception;
                throw new IllegalArgumentException("Invalid " + label + " syntax");
            }
        }
    }

    private boolean isLoopback(String host) {
        String normalized = host.toLowerCase(Locale.ROOT);
        return normalized.equals("localhost") || normalized.equals("127.0.0.1") || normalized.equals("::1") || normalized.equals("[::1]");
    }

    private int joinedLength(Set<String> values) { return values.stream().mapToInt(String::length).sum() + Math.max(0, values.size() - 1); }
    private void requireSet(Set<String> values, String label) { if (values == null || values.isEmpty()) throw new IllegalArgumentException(label + " are required"); }
    private void requireSetPresent(Set<String> values, String label) { if (values == null) throw new IllegalArgumentException(label + " are required"); }
    private boolean isDisabled(RegisteredClient client) { return Boolean.TRUE.equals(client.getClientSettings().getSetting(ActiveRegisteredClientRepository.DISABLED_SETTING)); }

    private ClientAuthenticationMethod authenticationMethod(String value) {
        return "client_secret_post".equals(value) ? ClientAuthenticationMethod.CLIENT_SECRET_POST : ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
    }
    private AuthorizationGrantType grantType(String value) {
        return switch (value) {
            case "authorization_code" -> AuthorizationGrantType.AUTHORIZATION_CODE;
            case "refresh_token" -> AuthorizationGrantType.REFRESH_TOKEN;
            case "client_credentials" -> AuthorizationGrantType.CLIENT_CREDENTIALS;
            default -> throw new IllegalArgumentException("Unsupported authorization grant type");
        };
    }
    private String generateSecret() { byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }

    private ClientDetails toDetails(RegisteredClient client) {
        return new ClientDetails(client.getClientId(), client.getClientName(),
                client.getClientAuthenticationMethods().stream().map(ClientAuthenticationMethod::getValue).sorted().toList(),
                client.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue).sorted().toList(),
                client.getRedirectUris().stream().sorted().toList(), client.getPostLogoutRedirectUris().stream().sorted().toList(),
                client.getScopes().stream().sorted().toList(), !isDisabled(client),
                client.getClientSettings().isRequireAuthorizationConsent(), client.getClientSettings().isRequireProofKey(),
                client.getTokenSettings().getAccessTokenTimeToLive().toSeconds(),
                client.getTokenSettings().getRefreshTokenTimeToLive().toSeconds(), client.getTokenSettings().isReuseRefreshTokens(),
                client.getClientIdIssuedAt());
    }

    public record ClientConfiguration(String clientName, Set<String> clientAuthenticationMethods,
                                      Set<String> authorizationGrantTypes, Set<String> redirectUris,
                                      Set<String> postLogoutRedirectUris, Set<String> scopes,
                                      boolean requireAuthorizationConsent, boolean requireProofKey,
                                      long accessTokenTtlSeconds, long refreshTokenTtlSeconds,
                                      boolean reuseRefreshTokens) {}
    public record ClientDetails(String clientId, String clientName, List<String> clientAuthenticationMethods,
                                List<String> authorizationGrantTypes, List<String> redirectUris,
                                List<String> postLogoutRedirectUris, List<String> scopes, boolean active,
                                boolean requireAuthorizationConsent, boolean requireProofKey,
                                long accessTokenTtlSeconds, long refreshTokenTtlSeconds, boolean reuseRefreshTokens,
                                java.time.Instant clientIdIssuedAt) {}
    public record CreatedClient(ClientDetails client, String clientSecret) {}
    public record ClientPage(List<ClientDetails> content, ClientPageMetadata page) {}
    public record ClientPageMetadata(int number, int size, long totalElements, int totalPages, boolean first, boolean last) {}
    public static class ClientNotFoundException extends RuntimeException { public ClientNotFoundException() { super("OAuth client not found"); } }
    public static class ClientConflictException extends RuntimeException { public ClientConflictException() { super("OAuth client configuration conflicts with an existing client"); } }
}
