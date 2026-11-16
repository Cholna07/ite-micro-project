package kh.edu.istad.platform.iam.web.config;

import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

@ControllerAdvice
public class TemplateModelAdvice {
    private final MessageSource messageSource;
    public TemplateModelAdvice(MessageSource messageSource) { this.messageSource = messageSource; }
    @ModelAttribute
    public void addI18n(Model model) {
        model.addAttribute("messages", messageSource);
        model.addAttribute("locale", LocaleContextHolder.getLocale());
    }
}
