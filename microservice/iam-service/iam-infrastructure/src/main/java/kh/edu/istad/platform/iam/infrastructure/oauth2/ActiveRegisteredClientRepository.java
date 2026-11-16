package kh.edu.istad.platform.iam.infrastructure.oauth2;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.Objects;

/** Enforces administrative soft-disable for client-id based OAuth lookups. */
final class ActiveRegisteredClientRepository implements RegisteredClientRepository {
    static final String DISABLED_SETTING = "northstar.disabled";
    private final RegisteredClientRepository delegate;

    ActiveRegisteredClientRepository(RegisteredClientRepository delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        delegate.save(registeredClient);
    }

    /** Keep disabled clients visible to authorization-history lookups by internal id. */
    @Override
    public RegisteredClient findById(String id) {
        return delegate.findById(id);
    }

    /** A disabled client cannot authenticate or start another authorization flow. */
    @Override
    public RegisteredClient findByClientId(String clientId) {
        RegisteredClient client = delegate.findByClientId(clientId);
        if (client == null || Boolean.TRUE.equals(client.getClientSettings().getSetting(DISABLED_SETTING))) {
            return null;
        }
        return client;
    }
}
