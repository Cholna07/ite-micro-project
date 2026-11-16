package kh.edu.istad.platform.iam.web;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class JteTemplateCompilationTest {
    @Test
    void allBrowserTemplatesCompileFromThePackagedResourcePath() {
        TemplateEngine engine = TemplateEngine.create(
                new ResourceCodeResolver("templates"), ContentType.Html);
        for (String template : List.of("login.jte", "register.jte", "consent.jte", "index.jte",
                "profile-edit.jte", "change-password.jte", "linked-accounts.jte", "connected-apps.jte")) {
            assertFalse(engine.getParamInfo(template).isEmpty(), template + " should compile with declared parameters");
        }
    }
}
