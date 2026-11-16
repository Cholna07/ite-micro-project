@param org.springframework.context.MessageSource messages
@param java.util.Locale locale
@param java.util.List<kh.edu.istad.platform.iam.domain.dto.result.IdentityResult> identities
@param boolean google
@param boolean github
@param boolean facebook
@param String csrfToken
@param String csrfParameter
<!doctype html><html lang="${locale.getLanguage()}"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${messages.getMessage("profile.linkedAccounts", null, locale)} · ${messages.getMessage("common.brand", null, locale)}</title><link rel="stylesheet" href="/css/iam.css"></head><body>
<header class="topbar"><a class="brand" href="/">${messages.getMessage("common.brand", null, locale)}</a><nav class="nav"><a href="/">${messages.getMessage("common.home", null, locale)}</a><a href="/profile/edit">${messages.getMessage("profile.edit", null, locale)}</a><a href="/profile/password">${messages.getMessage("profile.changePassword", null, locale)}</a><a href="/profile/accounts">${messages.getMessage("profile.linkedAccounts", null, locale)}</a><a href="/profile/apps">${messages.getMessage("profile.connectedApps", null, locale)}</a><a href="/profile/accounts?lang=en">${messages.getMessage("common.english", null, locale)}</a><a href="/profile/accounts?lang=km">${messages.getMessage("common.khmer", null, locale)}</a></nav></header>
<main class="page"><div class="page-heading"><div><h1>${messages.getMessage("profile.linkedAccounts", null, locale)}</h1><p class="muted">${messages.getMessage("accounts.description", null, locale)}</p></div></div>
    <section class="card"><h2>${messages.getMessage("accounts.linkedIdentities", null, locale)}</h2>
        @for(String provider : java.util.List.of("Google","GitHub","Facebook"))<div class="provider-card"><div class="provider-name"><span class="brand-mark">${provider.substring(0, 1)}</span><span>${provider}</span></div><span class="status @if((provider.equals("Google") && google) || (provider.equals("GitHub") && github) || (provider.equals("Facebook") && facebook))connected@endif">@if((provider.equals("Google") && google) || (provider.equals("GitHub") && github) || (provider.equals("Facebook") && facebook))${messages.getMessage("accounts.connected", null, locale)}@else${messages.getMessage("accounts.notConnected", null, locale)}@endif</span></div>@endfor
        @if(identities.isEmpty())<div class="empty-state">${messages.getMessage("accounts.none", null, locale)}</div>@else
            <div class="section-card">@for(var identity : identities)<div class="row"><span><b>${identity.provider()}</b> · @if(identity.email() != null)${identity.email()}@else${messages.getMessage("profile.emailUnavailable", null, locale)}@endif</span><form method="post" action="/profile/accounts/${String.valueOf(identity.uuid())}/unlink"><input type="hidden" name="${csrfParameter}" value="${csrfToken}"><button class="danger" type="submit">${messages.getMessage("accounts.unlink", null, locale)}</button></form></div>@endfor</div>
        @endif
        <p class="muted">${messages.getMessage("accounts.linkNotice", null, locale)}</p>
    </section>
</main><footer class="footer">${messages.getMessage("common.footer", null, locale)}</footer></body></html>
