package kh.edu.istad.platform.iam.domain.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a successful administrative command for audit recording. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditAction {
    String action();
    String targetType();
    int targetArgumentIndex() default -1;
    String targetProperty() default "uuid";
    String detailsProperty() default "";
}
