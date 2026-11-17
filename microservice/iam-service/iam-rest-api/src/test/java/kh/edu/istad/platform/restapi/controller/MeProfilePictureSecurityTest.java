package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.usecase.identity.ListUserIdentitiesUseCase;
import kh.edu.istad.platform.iam.domain.usecase.identity.UnlinkIdentityUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ChangePasswordUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.CurrentPasswordIncorrectException;
import kh.edu.istad.platform.iam.domain.usecase.user.DisableUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.GetCurrentUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListAccountActivityUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ListLoginHistoryUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.UpdateUserProfileUseCase;
import kh.edu.istad.platform.restapi.config.JwtAuthenticationConverter;
import kh.edu.istad.platform.restapi.config.SecurityConfig;
import kh.edu.istad.platform.restapi.exception.GlobalExceptionHandler;
import kh.edu.istad.platform.restapi.service.ManageCurrentProfilePictureUseCase;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeController.class)
@Import({SecurityConfig.class, MeController.class, GlobalExceptionHandler.class, MeProfilePictureSecurityTest.SecurityBeans.class})
@ContextConfiguration(classes = MeProfilePictureSecurityTest.TestApplication.class)
class MeProfilePictureSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean GetCurrentUserUseCase currentUser;
    @MockitoBean ListUserIdentitiesUseCase identities;
    @MockitoBean UnlinkIdentityUseCase unlinkIdentity;
    @MockitoBean DisableUserUseCase disableUser;
    @MockitoBean UpdateUserProfileUseCase updateProfile;
    @MockitoBean ChangePasswordUseCase changePassword;
    @MockitoBean ListLoginHistoryUseCase loginHistory;
    @MockitoBean ListAccountActivityUseCase activity;
    @MockitoBean ManageCurrentProfilePictureUseCase managePicture;
    @MockitoBean JwtDecoder jwtDecoder;
    @MockitoBean JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void profilePictureEndpointRequiresAuthentication() throws Exception {
        mvc.perform(multipart("/api/me/profile-picture").file("file", new byte[]{1})
                .with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(managePicture);
    }

    @Test
    void passwordEndpointRequiresAuthentication() throws Exception {
        mvc.perform(put("/api/me/password").with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"current\",\"newPassword\":\"long-password\",\"confirmPassword\":\"long-password\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(currentUser, changePassword);
    }

    @Test
    void incorrectCurrentPasswordReturnsSafeBadRequestMessage() throws Exception {
        UUID authenticatedUuid = UUID.randomUUID();
        when(currentUser.execute()).thenReturn(new UserResult(authenticatedUuid, "owner", null, null,
                null, null, null, null, null, false, false, true, false, null, null));
        doThrow(new CurrentPasswordIncorrectException()).when(changePassword).execute(any());

        mvc.perform(put("/api/me/password").with(user("owner")).with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrong\",\"newPassword\":\"long-password\",\"confirmPassword\":\"long-password\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail")
                        .value("Current password is incorrect"));
    }

    @Test
    void reproducesUploadFailureWhenJsonDefaultContentTypeReachesTheMultipartEndpoint() throws Exception {
        var result = mvc.perform(post("/api/me/profile-picture").contentType("application/json").content("{}")
                        .with(user("owner")).with(csrf()))
                .andReturn();
        org.junit.jupiter.api.Assertions.assertEquals(415, result.getResponse().getStatus());
        org.junit.jupiter.api.Assertions.assertEquals("", result.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertEquals("Content-Type 'application/json' is not supported.",
                result.getResponse().getErrorMessage());
        verifyNoInteractions(managePicture);
    }

    @Test
    void ignoresSuppliedOtherUserUuidAndUsesAuthenticatedCurrentUser() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        when(currentUser.execute()).thenReturn(new UserResult(owner, "owner", null, null, null,
                null, null, null, null, false, false, true, false, null, null));
        when(managePicture.upload(eq(owner), any(), any())).thenReturn(new UserResult(owner, "owner", null,
                null, null, null, null, null, null, false, false, true, false, null, null));

        mvc.perform(multipart("/api/me/profile-picture").file("file", new byte[]{1})
                        .param("userUuid", other.toString()).with(user("owner")).with(csrf()))
                .andExpect(status().isOk());
        verify(managePicture).upload(eq(owner), any(), any());
        verify(managePicture, never()).upload(eq(other), any(), any());
    }

    @TestConfiguration
    static class SecurityBeans {
        @Bean CookieCsrfTokenRepository csrfTokenRepository() { return CookieCsrfTokenRepository.withHttpOnlyFalse(); }
        @Bean @Primary Filter spaCsrfCookieFilter() { return passThrough(); }
        @Bean("sessionRevocationFilter") Filter sessionRevocationFilter() { return passThrough(); }
        private Filter passThrough() {
            return new OncePerRequestFilter() {
                @Override protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                        jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain chain)
                        throws jakarta.servlet.ServletException, IOException { chain.doFilter(request, response); }
            };
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableWebSecurity
    static class TestApplication { }
}
