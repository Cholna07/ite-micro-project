@param org.springframework.context.MessageSource messages
@param java.util.Locale locale
@param kh.edu.istad.platform.iam.domain.dto.result.UserResult user
@param java.util.Map<String,Object> claims
@param java.util.List<String> roles
@param java.util.List<String> permissions
@param String csrfToken
@param String csrfParameter
<!doctype html><html lang="${locale.getLanguage()}"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${messages.getMessage("profile.title", null, locale)} · ${messages.getMessage("common.brand", null, locale)}</title><link rel="stylesheet" href="/css/iam.css"></head><body>
<header class="topbar"><a class="brand" href="/">${messages.getMessage("common.brand", null, locale)}</a><nav class="nav" aria-label="${messages.getMessage("common.home", null, locale)}">
    <a href="/">${messages.getMessage("common.home", null, locale)}</a><a href="/profile/edit">${messages.getMessage("profile.edit", null, locale)}</a><a href="/profile/password">${messages.getMessage("profile.changePassword", null, locale)}</a><a href="/profile/accounts">${messages.getMessage("profile.linkedAccounts", null, locale)}</a><a href="/profile/apps">${messages.getMessage("profile.connectedApps", null, locale)}</a>
    <a href="/?lang=en">${messages.getMessage("common.english", null, locale)}</a><a href="/?lang=km">${messages.getMessage("common.khmer", null, locale)}</a>
    <form method="post" action="/logout"><input type="hidden" name="${csrfParameter}" value="${csrfToken}"><button class="secondary" type="submit">${messages.getMessage("common.logout", null, locale)}</button></form>
</nav></header>
<main class="page"><div class="page-heading"><div><h1>${messages.getMessage("common.welcome", null, locale)}, ${user.username()}</h1><p class="muted">${messages.getMessage("profile.homeIntro", null, locale)}</p></div><span class="brand-mark" aria-hidden="true">${user.username().substring(0, Math.min(1, user.username().length())).toUpperCase()}</span></div>
    <div class="grid"><section class="card"><h2>${messages.getMessage("profile.title", null, locale)}</h2><div class="row"><b>${messages.getMessage("register.username", null, locale)}</b><span>${user.username()}</span></div><div class="row"><b>${messages.getMessage("register.email", null, locale)}</b><span>@if(user.email() != null)${user.email()}@else${messages.getMessage("profile.emailUnavailable", null, locale)}@endif</span></div><div class="row"><b>${messages.getMessage("profile.givenName", null, locale)}</b><span>${user.givenName()}</span></div><div class="row"><b>${messages.getMessage("profile.familyName", null, locale)}</b><span>${user.familyName()}</span></div><div class="form-actions"><a class="button" href="/profile/edit">${messages.getMessage("profile.edit", null, locale)}</a><a class="button secondary" href="/profile/password">${messages.getMessage("profile.changePassword", null, locale)}</a></div></section>
    <section class="card"><h2>${messages.getMessage("profile.roles", null, locale)}</h2>@if(roles.isEmpty())<p class="muted">—</p>@else @for(String role : roles)<span class="pill">${role}</span>@endfor @endif<h2>${messages.getMessage("profile.permissions", null, locale)}</h2>@if(permissions.isEmpty())<p class="muted">—</p>@else @for(String permission : permissions)<span class="pill">${permission}</span>@endfor @endif</section></div>
    <section class="card section-card"><h2>${messages.getMessage("profile.claims", null, locale)}</h2>@for(var entry : claims.entrySet())<div class="row"><b>${entry.getKey()}</b><span>${String.valueOf(entry.getValue())}</span></div>@endfor</section>
</main><footer class="footer">${messages.getMessage("common.footer", null, locale)}</footer></body></html>
