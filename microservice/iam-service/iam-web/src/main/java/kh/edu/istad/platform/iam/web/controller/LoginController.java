package kh.edu.istad.platform.iam.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    private final Environment environment;

    public LoginController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/login")
    public String login(HttpServletResponse response) {
        String providers = List.of(
                        configured("GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET", "google") ? "google" : "",
                        configured("GITHUB_CLIENT_ID", "GITHUB_CLIENT_SECRET", "github") ? "github" : "",
                        configured("FACEBOOK_CLIENT_ID", "FACEBOOK_CLIENT_SECRET", "facebook") ? "facebook" : "",
                        configured("TELEGRAM_CLIENT_ID", "TELEGRAM_CLIENT_SECRET", "telegram") ? "telegram" : "")
                .stream().filter(value -> !value.isBlank()).collect(Collectors.joining(","));
        response.setHeader("X-Northstar-OAuth-Providers", providers);
        response.setHeader("Cache-Control", "no-store");
        return "forward:/frontend/index.html";
    }

    private boolean configured(String clientIdVariable, String clientSecretVariable, String registrationId) {
        String clientId = firstConfigured(
                readProperty(clientIdVariable),
                readProperty("spring.security.oauth2.client.registration." + registrationId + ".client-id")
        );
        String clientSecret = firstConfigured(
                readProperty(clientSecretVariable),
                readProperty("spring.security.oauth2.client.registration." + registrationId + ".client-secret")
        );
        return clientId != null && clientSecret != null;
    }

    private String readProperty(String name) {
        try {
            return environment.getProperty(name);
        } catch (IllegalArgumentException unresolvedPlaceholder) {
            return null;
        }
    }

    private String firstConfigured(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank() && !candidate.contains("${")) {
                return candidate;
            }
        }
        return null;
    }
}
