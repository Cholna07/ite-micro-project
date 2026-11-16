package kh.edu.istad.platform.iam.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

@Controller
public class LoginController {

    private final Environment environment;

    public LoginController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request,
                        @RequestAttribute(name = "_csrf", required = false) CsrfToken csrf,
                        Model model) {
        model.addAttribute("error", request.getParameter("error") != null);
        model.addAttribute("logout", request.getParameter("logout") != null);
        model.addAttribute("registered", request.getParameter("registered") != null);
        model.addAttribute("googleAvailable", configured("GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET", "google"));
        model.addAttribute("githubAvailable", configured("GITHUB_CLIENT_ID", "GITHUB_CLIENT_SECRET", "github"));
        model.addAttribute("facebookAvailable", configured("FACEBOOK_CLIENT_ID", "FACEBOOK_CLIENT_SECRET", "facebook"));
        model.addAttribute("telegramAvailable", configured("TELEGRAM_CLIENT_ID", "TELEGRAM_CLIENT_SECRET", "telegram"));
        model.addAttribute("csrfToken", csrf == null ? "" : csrf.getToken());
        model.addAttribute("csrfParameter", csrf == null ? "_csrf" : csrf.getParameterName());
        return "login";
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
