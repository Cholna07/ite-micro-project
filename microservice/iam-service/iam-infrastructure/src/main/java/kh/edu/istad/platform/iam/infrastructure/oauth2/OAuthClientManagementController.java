package kh.edu.istad.platform.iam.infrastructure.oauth2;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/clients")
public class OAuthClientManagementController {
    private final OAuthClientManagementService clients;

    public OAuthClientManagementController(OAuthClientManagementService clients) {
        this.clients = clients;
    }

    @GetMapping
    public OAuthClientManagementService.ClientPage list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "clientName") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "ASC") String sortDirection
    ) {
        return execute(() -> clients.list(keyword, page, size, sortBy, sortDirection));
    }

    @GetMapping("/{clientId}")
    public OAuthClientManagementService.ClientDetails get(@PathVariable("clientId") String clientId) {
        return execute(() -> clients.get(clientId));
    }

    @PostMapping
    public ResponseEntity<OAuthClientManagementService.CreatedClient> create(
            @Valid @RequestBody ClientConfigurationRequest request
    ) {
        var result = execute(() -> clients.create(request.toConfiguration()));
        return ResponseEntity.created(URI.create("/api/admin/clients/" + result.client().clientId())).body(result);
    }

    @PutMapping("/{clientId}")
    public OAuthClientManagementService.ClientDetails update(
            @PathVariable("clientId") String clientId,
            @Valid @RequestBody ClientConfigurationRequest request
    ) {
        return execute(() -> clients.update(clientId, request.toConfiguration()));
    }

    /** DELETE is a safe deactivation: registered client and consent history remain stored. */
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deactivate(@PathVariable("clientId") String clientId) {
        execute(() -> { clients.deactivate(clientId); return null; });
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{clientId}/activate")
    public OAuthClientManagementService.ClientDetails activate(@PathVariable("clientId") String clientId) {
        return execute(() -> clients.activate(clientId));
    }

    @PostMapping("/{clientId}/rotate-secret")
    public OAuthClientManagementService.CreatedClient rotateSecret(@PathVariable("clientId") String clientId) {
        return execute(() -> clients.rotateSecret(clientId));
    }

    private <T> T execute(java.util.function.Supplier<T> action) {
        try {
            return action.get();
        } catch (OAuthClientManagementService.ClientNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (OAuthClientManagementService.ClientConflictException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    public record ClientConfigurationRequest(
            @NotBlank String clientName,
            @NotEmpty Set<@NotBlank String> clientAuthenticationMethods,
            @NotEmpty Set<@NotBlank String> authorizationGrantTypes,
            @NotNull Set<@NotBlank String> redirectUris,
            @NotNull Set<@NotBlank String> postLogoutRedirectUris,
            @NotEmpty Set<@NotBlank String> scopes,
            boolean requireAuthorizationConsent,
            boolean requireProofKey,
            long accessTokenTtlSeconds,
            long refreshTokenTtlSeconds,
            boolean reuseRefreshTokens
    ) {
        OAuthClientManagementService.ClientConfiguration toConfiguration() {
            return new OAuthClientManagementService.ClientConfiguration(clientName, clientAuthenticationMethods,
                    authorizationGrantTypes, redirectUris, postLogoutRedirectUris, scopes,
                    requireAuthorizationConsent, requireProofKey, accessTokenTtlSeconds,
                    refreshTokenTtlSeconds, reuseRefreshTokens);
        }
    }
}
