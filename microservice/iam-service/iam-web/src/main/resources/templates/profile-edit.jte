@param org.springframework.context.MessageSource messages
@param java.util.Locale locale
@param kh.edu.istad.platform.iam.domain.dto.result.UserResult user
@param java.util.List<String> errors
@param boolean saved
@param String csrfToken
@param String csrfParameter
<!doctype html><html lang="${locale.getLanguage()}"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${messages.getMessage("profile.edit", null, locale)} · ${messages.getMessage("common.brand", null, locale)}</title><link rel="stylesheet" href="/css/iam.css"></head><body>
<header class="topbar"><a class="brand" href="/">${messages.getMessage("common.brand", null, locale)}</a><nav class="nav"><a href="/">${messages.getMessage("common.home", null, locale)}</a><a href="/profile/edit">${messages.getMessage("profile.edit", null, locale)}</a><a href="/profile/password">${messages.getMessage("profile.changePassword", null, locale)}</a><a href="/profile/accounts">${messages.getMessage("profile.linkedAccounts", null, locale)}</a><a href="/profile/apps">${messages.getMessage("profile.connectedApps", null, locale)}</a><a href="/profile/edit?lang=en">${messages.getMessage("common.english", null, locale)}</a><a href="/profile/edit?lang=km">${messages.getMessage("common.khmer", null, locale)}</a></nav></header>
<main class="page"><div class="page-heading"><div><h1>${messages.getMessage("profile.edit", null, locale)}</h1><p class="muted">${messages.getMessage("profile.accountIdentifiersReadOnly", null, locale)}</p></div></div>
    @if(saved)<div class="alert success" role="status">${messages.getMessage("profile.saved", null, locale)}</div>@endif
    @if(!errors.isEmpty())<div class="alert error" role="alert">@for(String error : errors)<div>${error}</div>@endfor</div>@endif
    <section class="card"><form method="post" action="/profile/edit"><input type="hidden" name="${csrfParameter}" value="${csrfToken}"><div class="grid">
        <div class="field"><label for="givenName">${messages.getMessage("profile.givenName", null, locale)}</label><input id="givenName" name="givenName" value="${user.givenName()}" autocomplete="given-name"></div>
        <div class="field"><label for="familyName">${messages.getMessage("profile.familyName", null, locale)}</label><input id="familyName" name="familyName" value="${user.familyName()}" autocomplete="family-name"></div>
        <div class="field"><label for="middleName">${messages.getMessage("profile.middleName", null, locale)}</label><input id="middleName" name="middleName" value="${user.middleName()}"></div>
        <div class="field"><label for="nickname">${messages.getMessage("profile.nickname", null, locale)}</label><input id="nickname" name="nickname" value="${user.nickname()}"></div>
        <div class="field"><label for="gender">${messages.getMessage("profile.gender", null, locale)}</label><input id="gender" name="gender" value="${user.gender()}"></div>
        <div class="field"><label for="phoneNumber">${messages.getMessage("profile.phone", null, locale)}</label><input id="phoneNumber" name="phoneNumber" value="${user.phoneNumber()}" autocomplete="tel"></div>
    </div><div class="form-actions"><button type="submit">${messages.getMessage("common.save", null, locale)}</button><a class="button secondary" href="/">${messages.getMessage("common.cancel", null, locale)}</a></div></form></section>
</main><footer class="footer">${messages.getMessage("common.footer", null, locale)}</footer></body></html>
