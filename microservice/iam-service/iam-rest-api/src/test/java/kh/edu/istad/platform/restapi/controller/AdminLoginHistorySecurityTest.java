package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.usecase.user.ListLoginHistoryUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListAccountActivityUseCase;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEvent;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordLoginEventUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListUserSessionsUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.RevokeUserSessionUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.GetCurrentUserUseCase;
import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.restapi.config.JwtAuthenticationConverter;
import kh.edu.istad.platform.restapi.config.SecurityConfig;
import kh.edu.istad.platform.restapi.exception.GlobalExceptionHandler;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AdminLoginHistoryController.class, AdminAccountActivityController.class, AdminUserSessionController.class, SessionController.class})
@Import({SecurityConfig.class, AdminLoginHistoryController.class, AdminAccountActivityController.class, AdminUserSessionController.class, SessionController.class, GlobalExceptionHandler.class, AdminLoginHistorySecurityTest.SecurityBeans.class})
@ContextConfiguration(classes = AdminLoginHistorySecurityTest.TestApplication.class)
class AdminLoginHistorySecurityTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ListLoginHistoryUseCase listLoginHistoryUseCase;
    @MockitoBean ListAccountActivityUseCase listAccountActivityUseCase;
    @MockitoBean ListUserSessionsUseCase listUserSessionsUseCase;
    @MockitoBean RevokeUserSessionUseCase revokeUserSessionUseCase;
    @MockitoBean GetCurrentUserUseCase getCurrentUserUseCase;
    @MockitoBean RecordLoginEventUseCase recordLoginEventUseCase;
    @MockitoBean RecordAccountActivityEventUseCase recordAccountActivityEventUseCase;
    @MockitoBean JwtDecoder jwtDecoder;
    @MockitoBean JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void rejectsUnauthenticatedAndNonAdministrators() throws Exception {
        mockMvc.perform(get("/api/admin/login-history")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/login-history").with(user("ordinary-user")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administrativeActivityRequiresDedicatedPermissionAndReturnsSafePagedEvents() throws Exception {
        UUID affectedUser = UUID.randomUUID();
        AccountActivityEvent event = new AccountActivityEvent(UUID.randomUUID(), affectedUser,
                AccountActivityEventType.PROFILE_UPDATED, "Profile information updated",
                java.time.Instant.parse("2026-01-01T12:00:00Z"), "192.0.2.10", "Example Browser");
        when(listAccountActivityUseCase.executeAll(0, 10))
                .thenReturn(new PageResult<>(List.of(event), 0, 10, 1, 1, true, true));

        mockMvc.perform(get("/api/admin/account-activities")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/account-activities").with(user("reader").authorities(new SimpleGrantedAuthority("user.read"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/account-activities").param("page", "0").param("size", "10")
                        .with(user("activity-reader").authorities(new SimpleGrantedAuthority("activity.read"))))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.content[0].userUuid").value(affectedUser.toString()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.content[0].description").value("Profile information updated"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.content[0].password").doesNotExist())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.content[0].accessToken").doesNotExist())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void userManagementReadAndMutationRoutesRequireTheirExistingAuthorities() throws Exception {
        UUID target = UUID.randomUUID();
        mockMvc.perform(get("/api/users").with(user("ordinary-user")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/users").with(user("reader").authorities(new SimpleGrantedAuthority("user.read"))))
                .andExpect(status().isNotFound()); // Passed security; no UserController is loaded in this slice.

        mockMvc.perform(post("/api/users").with(user("reader").authorities(new SimpleGrantedAuthority("user.read")))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/users/" + target + "/disable")
                        .with(user("reader").authorities(new SimpleGrantedAuthority("user.read")))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/users/" + target)
                        .with(user("reader").authorities(new SimpleGrantedAuthority("user.read")))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/users/" + target + "/disable")
                        .with(user("manager").authorities(new SimpleGrantedAuthority("user.manage")))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound()); // Passed security; no UserController is loaded in this slice.
    }

    @Test
    void userRoleAssignmentAndRoleOptionsRequireTheirDedicatedPermissions() throws Exception {
        UUID target = UUID.randomUUID();
        var csrf = org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf();

        mockMvc.perform(post("/api/users/" + target + "/roles")
                        .contentType("application/json").content("{\"roleUuids\":[]}")
                        .with(user("operator").authorities(new SimpleGrantedAuthority("user.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/users/" + target + "/roles")
                        .contentType("application/json").content("{\"roleUuids\":[]}")
                        .with(user("operator").authorities(new SimpleGrantedAuthority("user.role.assign"))).with(csrf))
                .andExpect(status().isNotFound()); // Authorized by the filter; UserController is not loaded in this slice.

        mockMvc.perform(get("/api/roles").with(user("operator").authorities(new SimpleGrantedAuthority("user.role.assign"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/roles").with(user("reader").authorities(new SimpleGrantedAuthority("role.read"))))
                .andExpect(status().isNotFound()); // Authorized by the filter; RoleController is not loaded in this slice.
    }

    @Test
    void directUserPermissionRoutesEnforceReadAndAssignmentAuthorities() throws Exception {
        UUID target = UUID.randomUUID();
        var csrf = org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf();

        mockMvc.perform(get("/api/users/" + target + "/permissions/direct")
                        .with(user("operator").authorities(new SimpleGrantedAuthority("user.permission.assign"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/users/" + target + "/permissions/direct")
                        .with(user("reader").authorities(new SimpleGrantedAuthority("user.read"))))
                .andExpect(status().isNotFound()); // Passed security; UserController is not loaded in this slice.

        mockMvc.perform(post("/api/users/" + target + "/permissions")
                        .contentType("application/json").content("{\"permissionUuids\":[],\"effect\":\"GRANT\"}")
                        .with(user("reader").authorities(new SimpleGrantedAuthority("user.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/users/" + target + "/permissions/" + UUID.randomUUID())
                        .with(user("reader").authorities(new SimpleGrantedAuthority("user.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/users/" + target + "/permissions")
                        .contentType("application/json").content("{\"permissionUuids\":[],\"effect\":\"GRANT\"}")
                        .with(user("operator").authorities(new SimpleGrantedAuthority("user.permission.assign"))).with(csrf))
                .andExpect(status().isNotFound()); // Passed security; UserController is not loaded in this slice.
    }

    @Test
    void roleRoutesRequireReadManageAndPermissionAssignmentAuthorities() throws Exception {
        UUID target = UUID.randomUUID();
        var csrf = org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf();

        mockMvc.perform(get("/api/roles").with(user("operator").authorities(new SimpleGrantedAuthority("role.manage"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/roles").with(user("reader").authorities(new SimpleGrantedAuthority("role.read"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/roles").contentType("application/json").content("{\"name\":\"VIEWER\"}")
                        .with(user("reader").authorities(new SimpleGrantedAuthority("role.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/roles/" + target).contentType("application/json").content("{\"name\":\"VIEWER\"}")
                        .with(user("reader").authorities(new SimpleGrantedAuthority("role.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/roles/" + target).with(user("reader").authorities(new SimpleGrantedAuthority("role.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/roles/" + target + "/deactivate").with(user("reader").authorities(new SimpleGrantedAuthority("role.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/roles").contentType("application/json").content("{\"name\":\"VIEWER\"}")
                        .with(user("manager").authorities(new SimpleGrantedAuthority("role.manage"))).with(csrf))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/roles/" + target + "/permissions").contentType("application/json").content("{\"permissionUuids\":[]}")
                        .with(user("manager").authorities(new SimpleGrantedAuthority("role.manage"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/roles/" + target + "/permissions/" + UUID.randomUUID())
                        .with(user("manager").authorities(new SimpleGrantedAuthority("role.manage"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/roles/" + target + "/permissions").contentType("application/json").content("{\"permissionUuids\":[]}")
                        .with(user("operator").authorities(new SimpleGrantedAuthority("role.permission.assign"))).with(csrf))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/permissions").with(user("operator").authorities(new SimpleGrantedAuthority("role.permission.assign"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/permissions").with(user("reader").authorities(new SimpleGrantedAuthority("permission.read"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void permissionRoutesRequireReadAndManageAuthoritiesForDirectRequests() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String body = "{\"name\":\"user.read\",\"resource\":\"user\",\"action\":\"read\"}";
        var csrf = org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf();
        mockMvc.perform(get("/api/permissions").with(user("operator").authorities(new SimpleGrantedAuthority("permission.manage"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/permissions/" + uuid).with(user("operator").authorities(new SimpleGrantedAuthority("permission.manage"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/permissions").contentType("application/json").content(body)
                        .with(user("reader").authorities(new SimpleGrantedAuthority("permission.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/permissions/" + uuid).contentType("application/json").content(body)
                        .with(user("reader").authorities(new SimpleGrantedAuthority("permission.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/permissions/" + uuid)
                        .with(user("reader").authorities(new SimpleGrantedAuthority("permission.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/permissions/" + uuid + "/activate")
                        .with(user("reader").authorities(new SimpleGrantedAuthority("permission.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/permissions/" + uuid + "/deactivate")
                        .with(user("reader").authorities(new SimpleGrantedAuthority("permission.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/permissions").with(user("reader").authorities(new SimpleGrantedAuthority("permission.read"))))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/permissions").contentType("application/json").content(body)
                        .with(user("manager").authorities(new SimpleGrantedAuthority("permission.manage"))).with(csrf))
                .andExpect(status().isNotFound());
    }

    @Test
    void oauthClientAdministrationRequiresDedicatedReadAndManageAuthorities() throws Exception {
        String clientId = "sample-client";
        var csrf = org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf();
        mockMvc.perform(get("/api/admin/clients").with(user("operator").authorities(new SimpleGrantedAuthority("client.manage"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/clients/" + clientId).with(user("operator").authorities(new SimpleGrantedAuthority("client.manage"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/clients").contentType("application/json").content("{}")
                        .with(user("reader").authorities(new SimpleGrantedAuthority("client.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/admin/clients/" + clientId).contentType("application/json").content("{}")
                        .with(user("reader").authorities(new SimpleGrantedAuthority("client.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/admin/clients/" + clientId)
                        .with(user("reader").authorities(new SimpleGrantedAuthority("client.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/admin/clients/" + clientId + "/activate")
                        .with(user("reader").authorities(new SimpleGrantedAuthority("client.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/clients/" + clientId + "/rotate-secret")
                        .with(user("reader").authorities(new SimpleGrantedAuthority("client.read"))).with(csrf))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/clients").with(user("reader").authorities(new SimpleGrantedAuthority("client.read"))))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/admin/clients").contentType("application/json").content("{}")
                        .with(user("manager").authorities(new SimpleGrantedAuthority("client.manage"))).with(csrf))
                .andExpect(status().isNotFound());
    }

    @Test
    void allowsUserReadPermissionAndPassesFiltersAndPagination() throws Exception {
        when(listLoginHistoryUseCase.execute(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 1, 5, 0, 0, false, true));

        mockMvc.perform(get("/api/admin/login-history")
                        .param("userUuid", "e63dc1f1-7df3-4c71-89b1-0bb08b82a2ab")
                        .param("outcome", "FAILURE").param("page", "1").param("size", "5")
                        .with(user("admin").authorities(new SimpleGrantedAuthority("user.read"))))
                .andExpect(status().isOk());
        org.mockito.Mockito.verify(listLoginHistoryUseCase).execute(
                UUID.fromString("e63dc1f1-7df3-4c71-89b1-0bb08b82a2ab"),
                kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome.FAILURE, 1, 5);
    }

    @Test
    void administratorSessionsRequireDedicatedReadPermission() throws Exception {
        UUID target = UUID.randomUUID();
        mockMvc.perform(get("/api/admin/users/" + target + "/sessions").with(user("ordinary-user")))
                .andExpect(status().isForbidden());
        when(listUserSessionsUseCase.execute(eq(target), eq(0), eq(50)))
                .thenReturn(new PageResult<>(List.of(), 0, 50, 0, 0, true, true));
        mockMvc.perform(get("/api/admin/users/" + target + "/sessions")
                        .with(user("admin").authorities(new SimpleGrantedAuthority("session.read"))))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/admin/users/" + target + "/sessions")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .with(user("admin").authorities(new SimpleGrantedAuthority("session.read"))))
                .andExpect(status().isForbidden());
        UUID admin = UUID.randomUUID();
        when(getCurrentUserUseCase.execute()).thenReturn(new UserResult(admin, "admin", null, null, null,
                null, null, null, null, false, false, true, false, null, null));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/admin/users/" + target + "/sessions")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .with(user("admin").authorities(new SimpleGrantedAuthority("session.revoke"))))
                .andExpect(status().isNoContent());
        org.mockito.Mockito.verify(revokeUserSessionUseCase).revokeAll(target, admin);
    }

    @Test
    void accountSessionEndpointsUseTheAuthenticatedOwnerAndNotARequestUserUuid() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(getCurrentUserUseCase.execute()).thenReturn(new UserResult(owner, "alice", null, null, null,
                null, null, null, null, false, false, true, false, null, null));
        when(listUserSessionsUseCase.execute(eq(owner), eq(0), eq(50)))
                .thenReturn(new PageResult<>(List.of(), 0, 50, 0, 0, true, true));

        mockMvc.perform(get("/api/me/sessions").param("userUuid", other.toString()).with(user("alice")))
                .andExpect(status().isOk());
        org.mockito.Mockito.verify(listUserSessionsUseCase).execute(owner, 0, 50);

        when(revokeUserSessionUseCase.execute(eq(owner), eq(sessionId), eq(owner), anyString())).thenReturn(false);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/me/sessions/" + sessionId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .with(user("alice")))
                .andExpect(status().isNotFound());
        org.mockito.Mockito.verify(revokeUserSessionUseCase).execute(owner, sessionId, owner, "REVOKED_BY_USER");
    }

    @Test
    void jwtCurrentSessionLogoutUsesTheSignedSessionClaimAndRequiresCsrf() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(getCurrentUserUseCase.execute()).thenReturn(new UserResult(owner, "alice", null, null, null,
                null, null, null, null, false, false, true, false, null, null));
        when(revokeUserSessionUseCase.execute(eq(owner), eq(sessionId), eq(owner), eq("LOGOUT"))).thenReturn(true);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/me/sessions/current")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .jwt().jwt(jwt -> jwt.subject(owner.toString()).claim("iam_session_id", sessionId.toString())))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
        org.mockito.Mockito.verify(revokeUserSessionUseCase).execute(owner, sessionId, owner, "LOGOUT");
    }

    @TestConfiguration
    static class SecurityBeans {
        @Bean CookieCsrfTokenRepository csrfTokenRepository() {
            return CookieCsrfTokenRepository.withHttpOnlyFalse();
        }

        @Bean @Primary Filter spaCsrfCookieFilter() {
            return new OncePerRequestFilter() {
                @Override protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                        jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain chain)
                        throws jakarta.servlet.ServletException, IOException {
                    chain.doFilter(request, response);
                }
            };
        }

        @Bean("sessionRevocationFilter") Filter sessionRevocationFilter() {
            return new OncePerRequestFilter() {
                @Override protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                        jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain chain)
                        throws jakarta.servlet.ServletException, IOException {
                    chain.doFilter(request, response);
                }
            };
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableWebSecurity
    static class TestApplication {
    }
}
