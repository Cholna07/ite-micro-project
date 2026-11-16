package kh.edu.istad.platform.iam.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Configuration
public class SpaCsrfConfiguration {

    @Bean
    CookieCsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    SpaCsrfCookieFilter spaCsrfCookieFilter() {
        return new SpaCsrfCookieFilter();
    }

    @Bean
    FilterRegistrationBean<SpaCsrfCookieFilter> disableContainerRegistration(
            SpaCsrfCookieFilter filter
    ) {
        FilterRegistrationBean<SpaCsrfCookieFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    static class SpaCsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            Object csrfAttribute = request.getAttribute(CsrfToken.class.getName());
            if (csrfAttribute instanceof CsrfToken csrfToken) {
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }

}
