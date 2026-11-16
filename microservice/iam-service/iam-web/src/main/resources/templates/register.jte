@param org.springframework.context.MessageSource messages
@param java.util.Locale locale
@param java.util.List<String> errors
@param String csrfToken
@param String csrfParameter
<!doctype html>
<html lang="${locale.getLanguage()}"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>${messages.getMessage("register.title", null, locale)} · ${messages.getMessage("common.brand", null, locale)}</title><link rel="stylesheet" href="/css/iam.css"><script defer src="/js/iam.js"></script></head><body>
<main class="auth-layout"><section class="card auth-card">
    <div class="language"><span>${messages.getMessage("common.language", null, locale)}</span><a href="/register?lang=en">${messages.getMessage("common.english", null, locale)}</a><a href="/register?lang=km">${messages.getMessage("common.khmer", null, locale)}</a></div>
    <div class="auth-brand"><span class="brand-mark"><svg width="21" height="21" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M12 3 19 6v5c0 4.7-3 8-7 10-4-2-7-5.3-7-10V6l7-3Z"/></svg></span>${messages.getMessage("common.brand", null, locale)}</div>
    <h1>${messages.getMessage("register.title", null, locale)}</h1>
    @if(!errors.isEmpty())<div class="alert error" role="alert"><ul class="list">@for(String error : errors)<li>${error}</li>@endfor</ul></div>@endif
    <form method="post" action="/register"><input type="hidden" name="${csrfParameter}" value="${csrfToken}">
        <div class="field"><label for="username">${messages.getMessage("register.username", null, locale)}</label><input id="username" name="username" autocomplete="username" required maxlength="100"></div>
        <div class="field"><label for="email">${messages.getMessage("register.email", null, locale)}</label><input id="email" name="email" type="email" autocomplete="email" required></div>
        <div class="field"><label for="password">${messages.getMessage("register.password", null, locale)}</label><div class="input-wrap"><input id="password" name="password" type="password" autocomplete="new-password" required><button type="button" class="toggle-password" data-password-toggle data-show-label="${messages.getMessage("common.showPassword", null, locale)}" data-hide-label="${messages.getMessage("common.hidePassword", null, locale)}" aria-label="${messages.getMessage("common.showPassword", null, locale)}" aria-pressed="false"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg></button></div></div>
        <div class="field"><label for="confirmPassword">${messages.getMessage("register.confirmPassword", null, locale)}</label><div class="input-wrap"><input id="confirmPassword" name="confirmPassword" type="password" autocomplete="new-password" required><button type="button" class="toggle-password" data-password-toggle data-show-label="${messages.getMessage("common.showPassword", null, locale)}" data-hide-label="${messages.getMessage("common.hidePassword", null, locale)}" aria-label="${messages.getMessage("common.showPassword", null, locale)}" aria-pressed="false"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg></button></div></div>
        <button class="full-width" type="submit">${messages.getMessage("register.submit", null, locale)}</button>
    </form><p class="muted">${messages.getMessage("register.haveAccount", null, locale)} <a href="/login">${messages.getMessage("common.backToLogin", null, locale)}</a></p>
</section><footer class="footer">${messages.getMessage("common.footer", null, locale)}</footer></main></body></html>
