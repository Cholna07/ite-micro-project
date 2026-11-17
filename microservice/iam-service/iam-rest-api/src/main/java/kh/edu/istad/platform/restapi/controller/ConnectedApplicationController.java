package kh.edu.istad.platform.restapi.controller;

import lombok.RequiredArgsConstructor;
import kh.edu.istad.platform.iam.domain.usecase.user.GetCurrentUserUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/me/connected-applications")
@RequiredArgsConstructor
public class ConnectedApplicationController {
    private final JdbcTemplate jdbcTemplate;
    private final GetCurrentUserUseCase currentUser;

    @GetMapping
    public List<ConnectedApplicationResponse> list() {
        String principalName = currentUser.execute().username();
        return jdbcTemplate.query("""
                SELECT c.registered_client_id, r.client_id, r.client_name, c.authorities
                FROM oauth2_authorization_consent c
                JOIN oauth2_registered_client r ON r.id = c.registered_client_id
                WHERE c.principal_name = ?
                ORDER BY r.client_name
                """, (rs, rowNum) -> new ConnectedApplicationResponse(
                rs.getString("registered_client_id"), rs.getString("client_id"),
                rs.getString("client_name"), parseScopes(rs.getString("authorities"))), principalName);
    }

    @DeleteMapping("/{registeredClientId}")
    @Transactional
    public void revoke(@PathVariable("registeredClientId") String registeredClientId) {
        String principalName = currentUser.execute().username();
        int removed = jdbcTemplate.update("""
                DELETE FROM oauth2_authorization_consent
                WHERE registered_client_id = ? AND principal_name = ?
                """, registeredClientId, principalName);
        if (removed == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Connected application was not found");
        }
        jdbcTemplate.update("""
                DELETE FROM oauth2_authorization
                WHERE registered_client_id = ? AND principal_name = ?
                """, registeredClientId, principalName);
    }

    private static List<String> parseScopes(String authorities) {
        if (authorities == null || authorities.isBlank()) return List.of();
        return Arrays.stream(authorities.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.startsWith("SCOPE_") ? value.substring("SCOPE_".length()) : value)
                .sorted()
                .toList();
    }

    public record ConnectedApplicationResponse(String id, String clientId, String name, List<String> scopes) { }
}
