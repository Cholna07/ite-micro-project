package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtelinkedaccountsGenerated {
	public static final String JTE_NAME = "linked-accounts.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,10,10,10,10,10,10,11,11,11,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,13,13,13,13,13,13,13,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,15,15,16,16,16,18,18,18,19,0,0,0,1,2,3,4,5,6,7,0,0,0};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, org.springframework.context.MessageSource messages, java.util.Locale locale, java.util.List<kh.edu.istad.platform.iam.domain.dto.result.IdentityResult> identities, boolean google, boolean github, boolean facebook, String csrfToken, String csrfParameter) {
		jteOutput.writeContent("<!doctype html><html");
		var __jte_html_attribute_0 = locale.getLanguage();
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
			jteOutput.writeContent(" lang=\"");
			jteOutput.setContext("html", "lang");
			jteOutput.writeUserContent(__jte_html_attribute_0);
			jteOutput.setContext("html", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent("><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>");
		jteOutput.setContext("title", null);
		jteOutput.writeUserContent(messages.getMessage("profile.linkedAccounts", null, locale));
		jteOutput.writeContent(" · ");
		jteOutput.setContext("title", null);
		jteOutput.writeUserContent(messages.getMessage("common.brand", null, locale));
		jteOutput.writeContent("</title><link rel=\"stylesheet\" href=\"/css/iam.css\"></head><body>\n<header class=\"topbar\"><a class=\"brand\" href=\"/\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.brand", null, locale));
		jteOutput.writeContent("</a><nav class=\"nav\"><a href=\"/\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.home", null, locale));
		jteOutput.writeContent("</a><a href=\"/profile/edit\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("profile.edit", null, locale));
		jteOutput.writeContent("</a><a href=\"/profile/password\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("profile.changePassword", null, locale));
		jteOutput.writeContent("</a><a href=\"/profile/accounts\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("profile.linkedAccounts", null, locale));
		jteOutput.writeContent("</a><a href=\"/profile/apps\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("profile.connectedApps", null, locale));
		jteOutput.writeContent("</a><a href=\"/profile/accounts?lang=en\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.english", null, locale));
		jteOutput.writeContent("</a><a href=\"/profile/accounts?lang=km\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.khmer", null, locale));
		jteOutput.writeContent("</a></nav></header>\n<main class=\"page\"><div class=\"page-heading\"><div><h1>");
		jteOutput.setContext("h1", null);
		jteOutput.writeUserContent(messages.getMessage("profile.linkedAccounts", null, locale));
		jteOutput.writeContent("</h1><p class=\"muted\">");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(messages.getMessage("accounts.description", null, locale));
		jteOutput.writeContent("</p></div></div>\n    <section class=\"card\"><h2>");
		jteOutput.setContext("h2", null);
		jteOutput.writeUserContent(messages.getMessage("accounts.linkedIdentities", null, locale));
		jteOutput.writeContent("</h2>\n        ");
		for (String provider : java.util.List.of("Google","GitHub","Facebook")) {
			jteOutput.writeContent("<div class=\"provider-card\"><div class=\"provider-name\"><span class=\"brand-mark\">");
			jteOutput.setContext("span", null);
			jteOutput.writeUserContent(provider.substring(0, 1));
			jteOutput.writeContent("</span><span>");
			jteOutput.setContext("span", null);
			jteOutput.writeUserContent(provider);
			jteOutput.writeContent("</span></div><span class=\"status ");
			if ((provider.equals("Google") && google) || (provider.equals("GitHub") && github) || (provider.equals("Facebook") && facebook)) {
				jteOutput.writeContent("connected");
			}
			jteOutput.writeContent("\">");
			if ((provider.equals("Google") && google) || (provider.equals("GitHub") && github) || (provider.equals("Facebook") && facebook)) {
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(messages.getMessage("accounts.connected", null, locale));
			} else {
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(messages.getMessage("accounts.notConnected", null, locale));
			}
			jteOutput.writeContent("</span></div>");
		}
		jteOutput.writeContent("\n        ");
		if (identities.isEmpty()) {
			jteOutput.writeContent("<div class=\"empty-state\">");
			jteOutput.setContext("div", null);
			jteOutput.writeUserContent(messages.getMessage("accounts.none", null, locale));
			jteOutput.writeContent("</div>");
		} else {
			jteOutput.writeContent("\n            <div class=\"section-card\">");
			for (var identity : identities) {
				jteOutput.writeContent("<div class=\"row\"><span><b>");
				jteOutput.setContext("b", null);
				jteOutput.writeUserContent(identity.provider());
				jteOutput.writeContent("</b> · ");
				if (identity.email() != null) {
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(identity.email());
				} else {
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(messages.getMessage("profile.emailUnavailable", null, locale));
				}
				jteOutput.writeContent("</span><form method=\"post\" action=\"/profile/accounts/");
				jteOutput.setContext("form", "action");
				jteOutput.writeUserContent(String.valueOf(identity.uuid()));
				jteOutput.setContext("form", null);
				jteOutput.writeContent("/unlink\"><input type=\"hidden\"");
				var __jte_html_attribute_1 = csrfParameter;
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
					jteOutput.writeContent(" name=\"");
					jteOutput.setContext("input", "name");
					jteOutput.writeUserContent(__jte_html_attribute_1);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				var __jte_html_attribute_2 = csrfToken;
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_2);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><button class=\"danger\" type=\"submit\">");
				jteOutput.setContext("button", null);
				jteOutput.writeUserContent(messages.getMessage("accounts.unlink", null, locale));
				jteOutput.writeContent("</button></form></div>");
			}
			jteOutput.writeContent("</div>\n        ");
		}
		jteOutput.writeContent("\n        <p class=\"muted\">");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(messages.getMessage("accounts.linkNotice", null, locale));
		jteOutput.writeContent("</p>\n    </section>\n</main><footer class=\"footer\">");
		jteOutput.setContext("footer", null);
		jteOutput.writeUserContent(messages.getMessage("common.footer", null, locale));
		jteOutput.writeContent("</footer></body></html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		org.springframework.context.MessageSource messages = (org.springframework.context.MessageSource)params.get("messages");
		java.util.Locale locale = (java.util.Locale)params.get("locale");
		java.util.List<kh.edu.istad.platform.iam.domain.dto.result.IdentityResult> identities = (java.util.List<kh.edu.istad.platform.iam.domain.dto.result.IdentityResult>)params.get("identities");
		boolean google = (boolean)params.get("google");
		boolean github = (boolean)params.get("github");
		boolean facebook = (boolean)params.get("facebook");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameter = (String)params.get("csrfParameter");
		render(jteOutput, jteHtmlInterceptor, messages, locale, identities, google, github, facebook, csrfToken, csrfParameter);
	}
}
