package kh.edu.istad.platform.iam.infrastructure.social;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public, non-secret provider discovery for the sign-in and linked-account screens. */
@RestController
@RequestMapping("/api/auth/providers")
public class SocialLoginProvidersController {
    private static final List<String> SUPPORTED_PROVIDERS = List.of("google", "github", "facebook", "telegram");

    private final ObjectProvider<ClientRegistrationRepository> registrationRepositories;

    public SocialLoginProvidersController(ObjectProvider<ClientRegistrationRepository> registrationRepositories) {
        this.registrationRepositories = registrationRepositories;
    }

    @GetMapping
    public List<String> listEnabledProviders() {
        ClientRegistrationRepository repository = registrationRepositories.getIfAvailable();
        if (repository == null) return List.of();
        return SUPPORTED_PROVIDERS.stream()
                .filter(provider -> repository.findByRegistrationId(provider) != null)
                .toList();
    }
}
