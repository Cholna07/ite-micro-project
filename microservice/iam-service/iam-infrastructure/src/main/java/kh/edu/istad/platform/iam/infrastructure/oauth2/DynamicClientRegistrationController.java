package kh.edu.istad.platform.iam.infrastructure.oauth2;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/connect/register")
public class DynamicClientRegistrationController {

    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public DynamicClientRegistrationController(
            RegisteredClientRepository registeredClientRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // POST: Register a new OAuth2 client
    @PostMapping
    public ResponseEntity<RegistrationResponse> registerClient(
            Authentication authentication,
            @Valid @RequestBody RegistrationRequest request
    ) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !hasClientCreateAuthority(authentication)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Missing client.create authority"
            );
        }

        validateRequest(request);

        String clientId = UUID.randomUUID().toString();
        String clientSecret = generateSecret();
        String registrationAccessToken = generateSecret();

        RegisteredClient registeredClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientName(request.client_name())
                .clientAuthenticationMethod(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                )
                .authorizationGrantType(
                        AuthorizationGrantType.AUTHORIZATION_CODE
                )
                .authorizationGrantType(
                        AuthorizationGrantType.REFRESH_TOKEN
                )
                .redirectUris(uris ->
                        uris.addAll(request.redirect_uris())
                )
                .scopes(scopes ->
                        scopes.addAll(request.scope())
                )
                .clientSettings(
                        ClientSettings.builder()
                                .setting(
                                        "registration_access_token_hash",
                                        passwordEncoder.encode(
                                                registrationAccessToken
                                        )
                                )
                                .build()
                )
                .tokenSettings(TokenSettings.builder().build())
                .build();

        registeredClientRepository.save(registeredClient);

        String registrationClientUri =
                ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/connect/register")
                        .queryParam("client_id", clientId)
                        .toUriString();

        RegistrationResponse response = new RegistrationResponse(
                clientId,
                clientSecret,
                request.client_name(),
                request.redirect_uris(),
                registrationAccessToken,
                registrationClientUri
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public RegistrationDetails getRegisteredClient(
            @RequestParam String client_id,
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            ) String authorization
    ) {

        System.out.println(">>> GET /connect/register CONTROLLER REACHED");

        System.out.println("Authorization header present: "
                + (authorization != null));

        System.out.println("Bearer prefix present: "
                + (authorization != null &&
                authorization.startsWith("Bearer ")));

        String registrationToken = extractBearerToken(authorization);

        RegisteredClient client =
                registeredClientRepository.findByClientId(client_id);

        if (client == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Client not found"
            );
        }

        Object storedHash = client.getClientSettings()
                .getSetting("registration_access_token_hash");

        boolean tokenMatches =
                storedHash instanceof String hash &&
                        passwordEncoder.matches(registrationToken, hash);

        System.out.println("Client found: true");
        System.out.println("Registration token matches: " + tokenMatches);

        if (!tokenMatches) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Invalid registration access token"
            );
        }

        return new RegistrationDetails(
                client.getClientId(),
                client.getClientName(),
                List.copyOf(client.getRedirectUris()),
                client.getScopes().stream().sorted().toList()
        );
    }

    private boolean hasClientCreateAuthority(
            Authentication authentication
    ) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("SCOPE_client.create") ||
                                authority.getAuthority().equals("client.create")
                );
    }

    // Validate registration request
    private void validateRequest(RegistrationRequest request) {

        if (!Set.of("authorization_code", "refresh_token")
                .containsAll(request.grant_types()) ||
                !request.grant_types().contains("authorization_code")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only authorization_code and refresh_token grants are supported"
            );
        }

        if (!request.response_types().equals(List.of("code"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only the code response type is supported"
            );
        }

        if (!"client_secret_basic".equals(
                request.token_endpoint_auth_method()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported token endpoint authentication method"
            );
        }

        if (!Set.of("openid", "profile", "email")
                .containsAll(request.scope()) ||
                !request.scope().contains("openid")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported scope"
            );
        }
    }

    private String extractBearerToken(String authorization) {

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Registration access token is required"
            );
        }

        return authorization.substring(7);
    }

    private String generateSecret() {

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public record RegistrationRequest(
            @NotBlank String client_name,
            @NotEmpty List<@NotBlank String> redirect_uris,
            @NotEmpty List<@NotBlank String> grant_types,
            @NotEmpty List<@NotBlank String> response_types,
            @NotEmpty List<@NotBlank String> scope,
            @NotBlank String token_endpoint_auth_method
    ) {}

    public record RegistrationResponse(
            String client_id,
            String client_secret,
            String client_name,
            List<String> redirect_uris,
            String registration_access_token,
            String registration_client_uri
    ) {}

    public record RegistrationDetails(
            String client_id,
            String client_name,
            List<String> redirect_uris,
            List<String> scope
    ) {}
}
