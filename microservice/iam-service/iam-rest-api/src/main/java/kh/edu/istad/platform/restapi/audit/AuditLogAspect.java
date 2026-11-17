package kh.edu.istad.platform.restapi.audit;

import kh.edu.istad.platform.iam.domain.audit.AuditAction;
import kh.edu.istad.platform.iam.domain.entity.AuditLogOutcome;
import kh.edu.istad.platform.iam.domain.usecase.audit.RecordAuditLogUseCase;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuditLogAspect {
    private static final System.Logger LOG = System.getLogger(AuditLogAspect.class.getName());
    private final RecordAuditLogUseCase recordAuditLog;

    public AuditLogAspect(RecordAuditLogUseCase recordAuditLog) {
        this.recordAuditLog = recordAuditLog;
    }

    @Around("@annotation(auditAction)")
    public Object audit(ProceedingJoinPoint invocation, AuditAction auditAction) throws Throwable {
        if (!isAuthenticatedAdministrativeRequest()) return invocation.proceed();
        Object result;
        try {
            result = invocation.proceed();
        } catch (Throwable failure) {
            if (!(failure instanceof IllegalArgumentException)) {
                String targetId = resolveTarget(invocation, auditAction, null);
                recordSafely(auditAction, targetId, AuditLogOutcome.FAILURE, failure.getClass().getSimpleName(), null);
            }
            throw failure;
        }
        String targetId = resolveTarget(invocation, auditAction, result);
        recordSafely(auditAction, targetId, AuditLogOutcome.SUCCESS, null, resolveDetails(auditAction, result));
        return result;
    }

    private String resolveDetails(AuditAction annotation, Object result) {
        if (result == null || annotation.detailsProperty().isBlank()) return null;
        Object value = result;
        for (String part : annotation.detailsProperty().split("\\.")) {
            value = readProperty(value, part);
            if (value == null) return null;
        }
        return value instanceof String text ? text : null;
    }

    private boolean isAuthenticatedAdministrativeRequest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return false;
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) return false;
        String path = attrs.getRequest().getRequestURI();
        String context = attrs.getRequest().getContextPath();
        if (context != null && !context.isEmpty() && path.startsWith(context)) path = path.substring(context.length());
        return path.startsWith("/api/admin/") || path.startsWith("/api/users/");
    }

    private String resolveTarget(ProceedingJoinPoint invocation, AuditAction annotation, Object result) {
        Object source = annotation.targetArgumentIndex() >= 0
                ? argumentAt(invocation, annotation.targetArgumentIndex()) : result;
        if (source == null) return null;
        if (source instanceof UUID || source instanceof String) return source.toString();
        String path = annotation.targetProperty();
        if (path == null || path.isBlank()) return null;
        Object value = source;
        for (String part : path.split("\\.")) {
            value = readProperty(value, part);
            if (value == null) return null;
        }
        return value instanceof UUID || value instanceof String ? value.toString() : null;
    }

    private Object argumentAt(ProceedingJoinPoint invocation, int index) {
        Object[] args = invocation.getArgs();
        return index >= 0 && index < args.length ? args[index] : null;
    }

    private Object readProperty(Object source, String property) {
        if (source == null) return null;
        String suffix = Character.toUpperCase(property.charAt(0)) + property.substring(1);
        for (String methodName : new String[]{property, "get" + suffix}) {
            try {
                Method method = source.getClass().getMethod(methodName);
                return method.invoke(source);
            } catch (ReflectiveOperationException ignored) {
                // Try the next public accessor shape.
            }
        }
        return null;
    }

    private void recordSafely(AuditAction annotation, String targetId, AuditLogOutcome outcome,
                              String failureCategory, String details) {
        if (targetId == null || targetId.isBlank()) {
            LOG.log(System.Logger.Level.WARNING, "Audit target could not be resolved for action {0}", annotation.action());
            return;
        }
        try {
            recordAuditLog.record(annotation.action(), annotation.targetType(), targetId, outcome, failureCategory, details);
        } catch (RuntimeException exception) {
            LOG.log(System.Logger.Level.ERROR, "Audit instrumentation failed for action {0} ({1})",
                    annotation.action(), exception.getClass().getSimpleName());
        }
    }
}
