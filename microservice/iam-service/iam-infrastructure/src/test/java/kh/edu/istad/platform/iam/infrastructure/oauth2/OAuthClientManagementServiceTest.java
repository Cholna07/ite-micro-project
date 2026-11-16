package kh.edu.istad.platform.iam.infrastructure.oauth2;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.port.out.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OAuthClientManagementServiceTest {
    private final InMemoryRegisteredClients storage = new InMemoryRegisteredClients();
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final PermissionRepository permissions = mock(PermissionRepository.class);
    private OAuthClientManagementService service;

    @BeforeEach
    void setUp() {
        when(permissions.findAll()).thenReturn(List.of());
        service = new OAuthClientManagementService(jdbc, storage, encoder, permissions);
    }

    @Test
    void createsClientWithEncodedSecretAndNoSecretInOrdinaryDetails() {
        var created = service.create(configuration("new client", Set.of("authorization_code", "refresh_token"),
                Set.of("openid", "profile"), Set.of("https://app.example/callback")));
        RegisteredClient persisted = storage.findByClientId(created.client().clientId());
        assertNotNull(persisted);
        assertNotEquals(created.clientSecret(), persisted.getClientSecret());
        assertTrue(encoder.matches(created.clientSecret(), persisted.getClientSecret()));
        assertEquals("new client", created.client().clientName());
        assertEquals(600, created.client().accessTokenTtlSeconds());
        assertFalse(Arrays.stream(created.client().getClass().getRecordComponents()).anyMatch(c -> c.getName().equals("clientSecret")));
        assertFalse(service.get(created.client().clientId()).toString().contains(created.clientSecret()));
    }

    @Test
    void updatesSupportedConfigurationWithoutChangingExistingSecret() {
        var created = service.create(configuration("before", Set.of("authorization_code"), Set.of("openid"), Set.of("https://app.example/cb")));
        RegisteredClient before = storage.findByClientId(created.client().clientId());
        String hash = before.getClientSecret();
        var updated = service.update(created.client().clientId(), configuration("after", Set.of("authorization_code", "refresh_token"),
                Set.of("openid", "email"), Set.of("https://app.example/new-cb")));
        assertEquals("after", updated.clientName());
        assertEquals(List.of("https://app.example/new-cb"), updated.redirectUris());
        assertEquals(hash, storage.findByClientId(created.client().clientId()).getClientSecret());
    }

    @Test
    void rejectsUnsupportedGrantScopeAndUnsafeRedirect() {
        assertThrows(IllegalArgumentException.class, () -> service.create(configuration("x", Set.of("implicit"), Set.of("openid"), Set.of("https://app.example/cb"))));
        assertThrows(IllegalArgumentException.class, () -> service.create(configuration("x", Set.of("authorization_code"), Set.of("admin:all"), Set.of("https://app.example/cb"))));
        assertThrows(IllegalArgumentException.class, () -> service.create(configuration("x", Set.of("authorization_code"), Set.of("openid"), Set.of("javascript:alert(1)"))));
        assertTrue(storage.clients.isEmpty());
    }

    @Test
    void deactivateHidesClientFromAuthorizationLookupAndReactivationRestoresNewFlows() {
        var created = service.create(configuration("client", Set.of("authorization_code"), Set.of("openid"), Set.of("https://app.example/cb")));
        ActiveRegisteredClientRepository authorizationLookup = new ActiveRegisteredClientRepository(storage);
        assertNotNull(authorizationLookup.findByClientId(created.client().clientId()));
        service.deactivate(created.client().clientId());
        assertNull(authorizationLookup.findByClientId(created.client().clientId()));
        assertFalse(service.get(created.client().clientId()).active());
        service.activate(created.client().clientId());
        assertNotNull(authorizationLookup.findByClientId(created.client().clientId()));
    }

    @Test
    void secretRotationReturnsNewSecretOnceAndStoresOnlyItsEncodedValue() {
        var created = service.create(configuration("client", Set.of("client_credentials"), Set.of("client.create"), Set.of()));
        var rotated = service.rotateSecret(created.client().clientId());
        RegisteredClient persisted = storage.findByClientId(created.client().clientId());
        assertNotEquals(created.clientSecret(), rotated.clientSecret());
        assertTrue(encoder.matches(rotated.clientSecret(), persisted.getClientSecret()));
        assertNotEquals(rotated.clientSecret(), persisted.getClientSecret());
    }

    @Test
    void listsUsingDatabasePaginationSearchAndSorting() {
        var created = service.create(configuration("searchable client", Set.of("client_credentials"), Set.of("client.create"), Set.of()));
        when(jdbc.queryForObject(contains("COUNT(*)"), eq(Long.class), any(Object[].class))).thenReturn(21L);
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<String>>any(), any(Object[].class)))
                .thenReturn(List.of(created.client().clientId()));
        var result = service.list("search", 1, 10, "clientName", "DESC");
        assertEquals(21, result.page().totalElements());
        assertEquals(3, result.page().totalPages());
        assertEquals(1, result.page().number());
        assertEquals(created.client().clientId(), result.content().get(0).clientId());
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sql.capture(), ArgumentMatchers.<RowMapper<String>>any(), any(Object[].class));
        assertTrue(sql.getValue().contains("POSITION(? IN LOWER(client_id))"));
        assertTrue(sql.getValue().contains("ORDER BY client_name DESC LIMIT ? OFFSET ?"));
    }

    private OAuthClientManagementService.ClientConfiguration configuration(String name, Set<String> grants, Set<String> scopes, Set<String> redirects) {
        return new OAuthClientManagementService.ClientConfiguration(name, Set.of("client_secret_basic"), grants, redirects,
                Set.of(), scopes, true, grants.contains("authorization_code"), 600, 3600, true);
    }

    private static class InMemoryRegisteredClients implements RegisteredClientRepository {
        private final Map<String, RegisteredClient> clients = new LinkedHashMap<>();
        @Override public void save(RegisteredClient client) { clients.put(client.getClientId(), client); }
        @Override public RegisteredClient findById(String id) { return clients.values().stream().filter(client -> client.getId().equals(id)).findFirst().orElse(null); }
        @Override public RegisteredClient findByClientId(String clientId) { return clients.get(clientId); }
    }
}
