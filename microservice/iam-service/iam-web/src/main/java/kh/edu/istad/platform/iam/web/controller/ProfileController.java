package kh.edu.istad.platform.iam.web.controller;

import kh.edu.istad.platform.iam.domain.dto.command.ChangePasswordCommand;
import kh.edu.istad.platform.iam.domain.dto.command.UpdateUserProfileCommand;
import kh.edu.istad.platform.iam.domain.dto.result.AuthUserResult;
import kh.edu.istad.platform.iam.domain.dto.result.IdentityResult;
import kh.edu.istad.platform.iam.domain.dto.result.UserAuthoritiesResult;
import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.usecase.identity.ListUserIdentitiesUseCase;
import kh.edu.istad.platform.iam.domain.usecase.identity.UnlinkIdentityUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.ChangePasswordUseCase;
import kh.edu.istad.platform.iam.domain.usecase.config.PasswordPolicyViolationException;
import kh.edu.istad.platform.iam.domain.usecase.user.GetUserAuthoritiesUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.GetUserUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.LoadUserForAuthenticationUseCase;
import kh.edu.istad.platform.iam.domain.usecase.user.UpdateUserProfileUseCase;
import org.springframework.security.core.Authentication;
import org.springframework.context.MessageSource;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;

@Controller
public class ProfileController {
    private final LoadUserForAuthenticationUseCase loadUser;
    private final GetUserUseCase getUser;
    private final GetUserAuthoritiesUseCase getAuthorities;
    private final UpdateUserProfileUseCase updateProfile;
    private final ChangePasswordUseCase changePassword;
    private final ListUserIdentitiesUseCase listIdentities;
    private final UnlinkIdentityUseCase unlinkIdentity;
    private final MessageSource messages;

    public ProfileController(LoadUserForAuthenticationUseCase loadUser, GetUserUseCase getUser,
                             GetUserAuthoritiesUseCase getAuthorities, UpdateUserProfileUseCase updateProfile,
                             ChangePasswordUseCase changePassword, ListUserIdentitiesUseCase listIdentities,
                             UnlinkIdentityUseCase unlinkIdentity, MessageSource messages) {
        this.loadUser = loadUser; this.getUser = getUser; this.getAuthorities = getAuthorities;
        this.updateProfile = updateProfile; this.changePassword = changePassword;
        this.listIdentities = listIdentities; this.unlinkIdentity = unlinkIdentity; this.messages = messages;
    }

    @GetMapping("/")
    public String home(Authentication authentication, @RequestAttribute(name = "_csrf", required = false) CsrfToken csrf, Model model) {
        return "forward:/frontend/index.html";
    }

    @GetMapping("/profile/edit")
    public String edit(Authentication authentication, Model model, @RequestAttribute(name = "_csrf", required = false) CsrfToken csrf) {
        return "forward:/frontend/index.html";
    }

    @PostMapping("/profile/edit")
    public String save(Authentication authentication, @RequestParam(name = "givenName", required = false) String givenName,
                       @RequestParam(name = "familyName", required = false) String familyName, @RequestParam(name = "middleName", required = false) String middleName,
                       @RequestParam(name = "nickname", required = false) String nickname, @RequestParam(name = "gender", required = false) String gender,
                       @RequestParam(name = "phoneNumber", required = false) String phoneNumber,
                       @RequestAttribute(name = "_csrf", required = false) CsrfToken csrf, Locale locale, Model model) {
        UUID uuid = account(authentication).uuid();
        List<String> errors = new java.util.ArrayList<>();
        if (givenName != null && givenName.length() > 100) errors.add(messages.getMessage("profile.givenNameTooLong", null, locale));
        if (familyName != null && familyName.length() > 100) errors.add(messages.getMessage("profile.familyNameTooLong", null, locale));
        if (phoneNumber != null && phoneNumber.length() > 32) errors.add(messages.getMessage("profile.phoneTooLong", null, locale));
        if (errors.isEmpty()) {
            try {
                updateProfile.execute(new UpdateUserProfileCommand(uuid, null, null, emptyToNull(givenName), emptyToNull(familyName),
                        emptyToNull(middleName), emptyToNull(nickname), emptyToNull(gender), null, null, null, null,
                        null, null, emptyToNull(phoneNumber), null, null, null, null, null));
                model.addAttribute("saved", true);
            } catch (IllegalArgumentException exception) { errors.add(messages.getMessage("profile.updateFailed", null, locale)); }
        }
        model.addAttribute("user", getUser.execute(uuid)); model.addAttribute("errors", errors);
        csrf(model, csrf); return "forward:/frontend/index.html";
    }

    @GetMapping("/profile/password")
    public String password(@RequestAttribute(name = "_csrf", required = false) CsrfToken csrf, Model model) {
        return "forward:/frontend/index.html";
    }

    @PostMapping("/profile/password")
    public String changePassword(Authentication authentication, @RequestParam(name = "currentPassword", required = false) String currentPassword,
                                 @RequestParam(name = "newPassword", required = false) String newPassword, @RequestParam(name = "confirmPassword", required = false) String confirmPassword,
                                 @RequestAttribute(name = "_csrf", required = false) CsrfToken csrf, Locale locale, Model model) {
        List<String> errors = new java.util.ArrayList<>();
        if (currentPassword == null || currentPassword.isBlank()) errors.add(messages.getMessage("password.currentRequired", null, locale));
        if (newPassword == null || newPassword.isBlank()) errors.add(messages.getMessage("password.newRequired", null, locale));
        if (!java.util.Objects.equals(newPassword, confirmPassword)) errors.add(messages.getMessage("password.mismatch", null, locale));
        if (errors.isEmpty()) {
            try { changePassword.execute(new ChangePasswordCommand(account(authentication).uuid(), currentPassword, newPassword));
                model.addAttribute("saved", true);
            } catch (PasswordPolicyViolationException exception) {
                String key = exception.tooLong() ? "validation.passwordMaximumLength" : "validation.passwordMinimumLength";
                Object[] args = exception.tooLong() ? new Object[]{exception.maximumLength()} : new Object[]{exception.minimumLength()};
                errors.add(messages.getMessage(key, args, locale));
            } catch (IllegalArgumentException exception) { errors.add(messages.getMessage("password.changeFailed", null, locale)); }
        }
        model.addAttribute("errors", errors); csrf(model, csrf); return "forward:/frontend/index.html";
    }

    @GetMapping("/profile/accounts")
    public String accounts(Authentication authentication, @RequestAttribute(name = "_csrf", required = false) CsrfToken csrf, Model model) {
        return "forward:/frontend/index.html";
    }

    @PostMapping("/profile/accounts/{identityUuid}/unlink")
    public String unlink(Authentication authentication, @PathVariable("identityUuid") UUID identityUuid) {
        unlinkIdentity.execute(account(authentication).uuid(), identityUuid);
        return "redirect:/profile/accounts";
    }

    @GetMapping("/profile/apps")
    public String apps(Model model) { return "forward:/frontend/index.html"; }

    private AuthUserResult account(Authentication authentication) {
        return loadUser.execute(authentication.getName());
    }
    private void csrf(Model model, CsrfToken csrf) {
        model.addAttribute("csrfToken", csrf == null ? "" : csrf.getToken());
        model.addAttribute("csrfParameter", csrf == null ? "_csrf" : csrf.getParameterName());
    }
    private String emptyToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
