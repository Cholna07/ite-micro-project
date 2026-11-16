@param org.springframework.context.MessageSource messages
@param java.util.Locale locale
@param boolean error
@param boolean logout
@param boolean registered
@param boolean googleAvailable
@param boolean githubAvailable
@param boolean facebookAvailable
@param boolean telegramAvailable
@param String csrfToken
@param String csrfParameter
<!doctype html>
<html lang="${locale.getLanguage()}">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>${messages.getMessage("login.title", null, locale)} · ${messages.getMessage("common.brand", null, locale)}</title>
    <link rel="stylesheet" href="/css/iam.css">
    <script defer src="/js/iam.js"></script>
</head>
<body>
<main class="auth-layout"><section class="card auth-card">
    <div class="language"><span>${messages.getMessage("common.language", null, locale)}</span><a href="/login?lang=en">${messages.getMessage("common.english", null, locale)}</a><a href="/login?lang=km">${messages.getMessage("common.khmer", null, locale)}</a></div>
    <div class="auth-brand"><span class="brand-mark"><svg width="21" height="21" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M12 3 19 6v5c0 4.7-3 8-7 10-4-2-7-5.3-7-10V6l7-3Z"/><path d="m9 12 2 2 4-4"/></svg></span>${messages.getMessage("common.brand", null, locale)}</div>
    <h1>${messages.getMessage("login.title", null, locale)}</h1><p class="auth-intro">${messages.getMessage("login.subtitle", null, locale)}</p>
    @if(error)<div class="alert error" role="alert">${messages.getMessage("login.error", null, locale)}</div>@endif
    @if(logout)<div class="alert success" role="status">${messages.getMessage("login.logoutSuccess", null, locale)}</div>@endif
    @if(registered)<div class="alert success" role="status">${messages.getMessage("register.success", null, locale)}</div>@endif
    <form method="post" action="/login"><input type="hidden" name="${csrfParameter}" value="${csrfToken}">
        <div class="field"><label for="username">${messages.getMessage("login.username", null, locale)}</label><input id="username" name="username" autocomplete="username" required autofocus></div>
        <div class="field"><label for="password">${messages.getMessage("login.password", null, locale)}</label><div class="input-wrap"><input id="password" name="password" type="password" autocomplete="current-password" required><button type="button" class="toggle-password" data-password-toggle data-show-label="${messages.getMessage("common.showPassword", null, locale)}" data-hide-label="${messages.getMessage("common.hidePassword", null, locale)}" aria-label="${messages.getMessage("common.showPassword", null, locale)}" aria-pressed="false"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg></button></div></div>
        <button class="full-width" type="submit">${messages.getMessage("login.submit", null, locale)}</button>
    </form>
    @if(googleAvailable || githubAvailable || facebookAvailable || telegramAvailable)<div class="divider">${messages.getMessage("login.continueWith", null, locale)}</div><div class="social">
        @if(googleAvailable)<a class="button secondary" href="/oauth2/authorization/google"><span class="provider-icon" aria-hidden="true">G</span>${messages.getMessage("login.google", null, locale)}</a>@endif
        @if(githubAvailable)<a class="button secondary" href="/oauth2/authorization/github"><span class="provider-icon" aria-hidden="true">GH</span>${messages.getMessage("login.github", null, locale)}</a>@endif
        @if(facebookAvailable)<a class="button secondary" href="/oauth2/authorization/facebook"><span class="provider-icon" aria-hidden="true">f</span>${messages.getMessage("login.facebook", null, locale)}</a>@endif
        @if(telegramAvailable)<a class="button secondary" href="/oauth2/authorization/telegram"><span class="provider-icon" aria-hidden="true">↗</span>${messages.getMessage("login.telegram", null, locale)}</a>@endif
    </div>@else<p class="muted">${messages.getMessage("login.socialUnavailable", null, locale)}</p>@endif
    <p class="muted">${messages.getMessage("login.noAccount", null, locale)} <a href="/register">${messages.getMessage("login.register", null, locale)}</a></p>
    <p class="muted"><a href="/login">${messages.getMessage("login.forgotPassword", null, locale)}</a></p>
</section><footer class="footer">${messages.getMessage("common.footer", null, locale)}</footer></main>
</body></html>
