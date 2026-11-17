package kh.edu.istad.platform.restapi.audit;

import kh.edu.istad.platform.iam.domain.audit.AuditAction;
import kh.edu.istad.platform.iam.domain.entity.AuditLogOutcome;
import kh.edu.istad.platform.iam.domain.usecase.audit.RecordAuditLogUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuditLogAspectTest {
    private final RecordAuditLogUseCase recordAuditLog = mock(RecordAuditLogUseCase.class);

    @AfterEach
    void clearRequestAndSecurityContext() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesAnnotationAndRecordsAuthenticatedAdministrativeSuccessThroughSpringAop() {
        setAuthenticatedAdminRequest();
        AuditedTarget proxy = proxiedTarget();
        UUID targetId = UUID.randomUUID();

        assertEquals(targetId, proxy.execute(targetId));

        verify(recordAuditLog).record("AOP_TEST", "TEST_TARGET", targetId.toString(),
                AuditLogOutcome.SUCCESS, null, null);
    }

    @Test
    void resolvesAnnotationAndRecordsAuthenticatedAdministrativeFailureThroughSpringAop() {
        setAuthenticatedAdminRequest();
        AuditedTarget proxy = proxiedTarget();
        UUID targetId = UUID.randomUUID();

        assertThrows(IllegalStateException.class, () -> proxy.fail(targetId));

        verify(recordAuditLog).record("AOP_TEST_FAILURE", "TEST_TARGET", targetId.toString(),
                AuditLogOutcome.FAILURE, "IllegalStateException", null);
    }

    @Test
    void unannotatedMethodsProceedWithoutAudit() {
        setAuthenticatedAdminRequest();
        AuditedTarget proxy = proxiedTarget();

        assertEquals("ordinary", proxy.unannotated());

        verifyNoInteractions(recordAuditLog);
    }

    @Test
    void annotatedMethodOutsideAuthenticatedAdministrativeRequestProceedsWithoutAudit() {
        AuditedTarget proxy = proxiedTarget();
        UUID targetId = UUID.randomUUID();

        assertEquals(targetId, proxy.execute(targetId));

        verifyNoInteractions(recordAuditLog);
    }

    private AuditedTarget proxiedTarget() {
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new AuditedTarget());
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAspect(new AuditLogAspect(recordAuditLog));
        return proxyFactory.getProxy();
    }

    private void setAuthenticatedAdminRequest() {
        var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new TestingAuthenticationToken("admin", "secret", "ROLE_ADMIN"));
        SecurityContextHolder.setContext(securityContext);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/aop-test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    static class AuditedTarget {
        @AuditAction(action = "AOP_TEST", targetType = "TEST_TARGET", targetArgumentIndex = 0)
        public UUID execute(UUID targetId) {
            return targetId;
        }

        @AuditAction(action = "AOP_TEST_FAILURE", targetType = "TEST_TARGET", targetArgumentIndex = 0)
        public void fail(UUID targetId) {
            throw new IllegalStateException("test failure");
        }

        public String unannotated() {
            return "ordinary";
        }
    }
}
