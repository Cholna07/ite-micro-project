package kh.edu.istad.platform.iam.infrastructure.social;

import kh.edu.istad.platform.iam.domain.usecase.identity.IdentityLinkSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IdentityLinkRequestContextTest {
    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void callbackUsesTheAccountBoundToItsOAuthSession() {
        UUID boundAccount = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(IdentityLinkSession.USER_UUID_ATTRIBUTE, boundAccount.toString());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals(boundAccount, IdentityLinkRequestContext.pendingUserUuid());
    }

    @Test
    void ordinarySocialLoginHasNoExplicitLinkBinding() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertNull(IdentityLinkRequestContext.pendingUserUuid());
    }
}
