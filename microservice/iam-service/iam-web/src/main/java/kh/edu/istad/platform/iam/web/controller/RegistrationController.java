package kh.edu.istad.platform.iam.web.controller;

import kh.edu.istad.platform.iam.domain.dto.command.RegisterUserCommand;
import kh.edu.istad.platform.iam.domain.usecase.user.RegisterUserUseCase;
import org.springframework.context.MessageSource;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Controller
public class RegistrationController {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final RegisterUserUseCase registerUser;
    private final MessageSource messages;

    public RegistrationController(RegisterUserUseCase registerUser, MessageSource messages) {
        this.registerUser = registerUser;
        this.messages = messages;
    }

    @GetMapping("/register")
    public String page(@RequestAttribute(name = "_csrf", required = false) CsrfToken csrf, Model model) {
        model.addAttribute("errors", List.of());
        model.addAttribute("csrfToken", csrf == null ? "" : csrf.getToken());
        model.addAttribute("csrfParameter", csrf == null ? "_csrf" : csrf.getParameterName());
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam(name = "username", required = false) String username,
                           @RequestParam(name = "email", required = false) String email,
                           @RequestParam(name = "password", required = false) String password,
                           @RequestParam(name = "confirmPassword", required = false) String confirmPassword,
                           @RequestAttribute(name = "_csrf", required = false) CsrfToken csrf,
                           Locale locale, Model model) {
        List<String> errors = new java.util.ArrayList<>();
        if (blank(username)) errors.add(messages.getMessage("validation.usernameRequired", null, locale));
        if (blank(email)) errors.add(messages.getMessage("validation.emailRequired", null, locale));
        else if (!EMAIL.matcher(email.trim()).matches()) errors.add(messages.getMessage("validation.emailInvalid", null, locale));
        if (blank(password)) errors.add(messages.getMessage("validation.passwordRequired", null, locale));
        if (!java.util.Objects.equals(password, confirmPassword)) errors.add(messages.getMessage("validation.passwordMismatch", null, locale));
        if (!errors.isEmpty()) {
            setFormModel(model, csrf, errors);
            return "register";
        }
        try {
            registerUser.execute(new RegisterUserCommand(username.trim(), email.trim(), password, null, null));
            return "redirect:/login?registered";
        } catch (IllegalStateException exception) {
            String key = exception.getMessage() != null && exception.getMessage().toLowerCase(Locale.ROOT).contains("email")
                    ? "validation.emailTaken" : "validation.usernameTaken";
            setFormModel(model, csrf, List.of(messages.getMessage(key, null, locale)));
            return "register";
        } catch (IllegalArgumentException exception) {
            setFormModel(model, csrf, List.of(messages.getMessage("validation.registrationFailed", null, locale)));
            return "register";
        }
    }

    private void setFormModel(Model model, CsrfToken csrf, List<String> errors) {
        model.addAttribute("errors", errors);
        model.addAttribute("csrfToken", csrf == null ? "" : csrf.getToken());
        model.addAttribute("csrfParameter", csrf == null ? "_csrf" : csrf.getParameterName());
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
