@param org.springframework.context.MessageSource messages
@param java.util.Locale locale
@param String clientId
@param String clientName
@param String state
@param java.util.List<String> scopes
@param String csrfToken
@param String csrfParameter
<!doctype html><html lang="${locale.getLanguage()}"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${messages.getMessage("consent.title", null, locale)}</title><link rel="stylesheet" href="/css/iam.css"></head><body>
<main class="auth-layout"><section class="card auth-card">
    <form method="get" action="/oauth2/consent" class="language"><input type="hidden" name="client_id" value="${clientId}"><input type="hidden" name="state" value="${state}">@for(String item : scopes)<input type="hidden" name="scope" value="${item}">@endfor
        <span>${messages.getMessage("common.language", null, locale)}</span><button class="secondary" name="lang" value="en">${messages.getMessage("common.english", null, locale)}</button><button class="secondary" name="lang" value="km">${messages.getMessage("common.khmer", null, locale)}</button>
    </form>
    <div class="auth-brand"><span class="brand-mark"><svg width="21" height="21" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M12 3 19 6v5c0 4.7-3 8-7 10-4-2-7-5.3-7-10V6l7-3Z"/><path d="M12 8v4m0 4h.01"/></svg></span>${messages.getMessage("common.brand", null, locale)}</div>
    <h1>${messages.getMessage("consent.title", null, locale)}</h1><p class="auth-intro">${messages.getMessage("consent.request", new Object[]{clientName}, locale)}</p>
    <div class="provider-card"><div class="provider-name"><span class="brand-mark"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M8 9h8M8 13h5"/></svg></span><span>${clientName}</span></div><span class="status">${messages.getMessage("consent.clientId", null, locale)}: ${clientId}</span></div>
    <h2>${messages.getMessage("consent.scopes", null, locale)}</h2><ul class="scope-list">@for(String item : scopes)<li>${item}</li>@endfor</ul>
    <form method="post" action="/oauth2/authorize"><input type="hidden" name="${csrfParameter}" value="${csrfToken}"><input type="hidden" name="client_id" value="${clientId}"><input type="hidden" name="state" value="${state}">@for(String item : scopes)<input type="hidden" name="scope" value="${item}">@endfor
        <div class="form-actions"><button type="submit" class="full-width" name="user_oauth_approval" value="true">${messages.getMessage("consent.allow", null, locale)}</button><button type="submit" class="secondary full-width" name="user_oauth_approval" value="false">${messages.getMessage("consent.cancel", null, locale)}</button></div>
    </form>
</section><footer class="footer">${messages.getMessage("common.footer", null, locale)}</footer></main></body></html>
