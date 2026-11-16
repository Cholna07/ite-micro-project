package kh.edu.istad.platform.iam.web.config;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class JteConfig {

    @Bean
    public TemplateEngine templateEngine() {
        ResourceCodeResolver resolver = new ResourceCodeResolver("templates");

        return TemplateEngine.create(
                resolver,
                ContentType.Html
        );
    }
}
