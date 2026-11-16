package kh.edu.istad.platform.iam.infrastructure.web;

import kh.edu.istad.platform.iam.domain.port.out.RequestMetadataProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ServletRequestMetadataProvider implements RequestMetadataProvider {
    @Override
    public String remoteAddress() {
        return request().map(attributes -> attributes.getRequest().getRemoteAddr()).orElse(null);
    }

    @Override
    public String userAgent() {
        return request().map(attributes -> attributes.getRequest().getHeader("User-Agent")).orElse(null);
    }

    private java.util.Optional<ServletRequestAttributes> request() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes
                ? java.util.Optional.of(attributes) : java.util.Optional.empty();
    }
}
