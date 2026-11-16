package kh.edu.istad.platform.iam.web;

import org.springframework.core.io.ClassPathResource;
import kh.edu.istad.platform.iam.web.controller.ReactApplicationController;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReactApplicationTest {
    @Test
    void packagedReactEntryPointContainsOnlyTheReactMountPoint() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/frontend/index.html");
        assertTrue(resource.exists(), "Vite must package the React entry point");
        String html;
        try (var input = resource.getInputStream()) {
            html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertTrue(html.contains("id=\"root\""));
        assertTrue(html.contains("/frontend/assets/iam-frontend.js"));
    }

    @Test
    void browserRoutesForwardToTheReactEntryPoint() {
        assertTrue(new ReactApplicationController().application().equals("forward:/frontend/index.html"));
    }
}
