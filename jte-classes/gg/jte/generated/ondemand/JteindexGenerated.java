package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteindexGenerated {
	public static final String JTE_NAME = "index.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,9,9,9,9,9,9,9,9,9,9,9,9,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,11,11,11,11,11,11,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,14,14,14,14,14,14,14,14,14,14,14,14,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,17,17,17,17,17,17,17,17,17,17,17,17,17,18,18,18,19,0,0,0,1,2,3,4,5,6,7,0,0,0};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, org.springframework.context.MessageSource messages, java.util.Locale locale, kh.edu.istad.platform.iam.domain.dto.result.UserResult user, java.util.Map<String,Object> claims, java.util.List<String> roles, java.util.List<String> permissions, String csrfToken, String csrfParameter) {
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
		jteOutput.writeUserContent(messages.getMessage("profile.title", null, locale));
		jteOutput.writeContent(" · ");
		jteOutput.setContext("title", null);
		jteOutput.writeUserContent(messages.getMessage("common.brand", null, locale));
		jteOutput.writeContent("</title><link rel=\"stylesheet\" href=\"/css/iam.css\"></head><body>\n<header class=\"topbar\"><a class=\"brand\" href=\"/\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.brand", null, locale));
		jteOutput.writeContent("</a><nav class=\"nav\"");
		var __jte_html_attribute_1 = messages.getMessage("common.home", null, locale);
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
			jteOutput.writeContent(" aria-label=\"");
			jteOutput.setContext("nav", "aria-label");
			jteOutput.writeUserContent(__jte_html_attribute_1);
			jteOutput.setContext("nav", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">\n    <a href=\"/\">");
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
		jteOutput.writeContent("</a>\n    <a href=\"/?lang=en\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.english", null, locale));
		jteOutput.writeContent("</a><a href=\"/?lang=km\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.khmer", null, locale));
		jteOutput.writeContent("</a>\n    <form method=\"post\" action=\"/logout\"><input type=\"hidden\"");
		var __jte_html_attribute_2 = csrfParameter;
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
			jteOutput.writeContent(" name=\"");
			jteOutput.setContext("input", "name");
			jteOutput.writeUserContent(__jte_html_attribute_2);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		var __jte_html_attribute_3 = csrfToken;
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_3)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_3);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent("><button class=\"secondary\" type=\"submit\">");
		jteOutput.setContext("button", null);
		jteOutput.writeUserContent(messages.getMessage("common.logout", null, locale));
		jteOutput.writeContent("</button></form>\n</nav></header>\n<main class=\"page\"><div class=\"page-heading\"><div><h1>");
		jteOutput.setContext("h1", null);
		jteOutput.writeUserContent(messages.getMessage("common.welcome", null, locale));
		jteOutput.writeContent(", ");
		jteOutput.setContext("h1", null);
		jteOutput.writeUserContent(user.username());
		jteOutput.writeContent("</h1><p class=\"muted\">");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(messages.getMessage("profile.homeIntro", null, locale));
		jteOutput.writeContent("</p></div><span class=\"brand-mark\" aria-hidden=\"true\">");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(user.username().substring(0, Math.min(1, user.username().length())).toUpperCase());
		jteOutput.writeContent("</span></div>\n    <div class=\"grid\"><section class=\"card\"><h2>");
		jteOutput.setContext("h2", null);
		jteOutput.writeUserContent(messages.getMessage("profile.title", null, locale));
		jteOutput.writeContent("</h2><div class=\"row\"><b>");
		jteOutput.setContext("b", null);
		jteOutput.writeUserContent(messages.getMessage("register.username", null, locale));
		jteOutput.writeContent("</b><span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(user.username());
		jteOutput.writeContent("</span></div><div class=\"row\"><b>");
		jteOutput.setContext("b", null);
		jteOutput.writeUserContent(messages.getMessage("register.email", null, locale));
		jteOutput.writeContent("</b><span>");
		if (user.email() != null) {
			jteOutput.setContext("span", null);
			jteOutput.writeUserContent(user.email());
		} else {
			jteOutput.setContext("span", null);
			jteOutput.writeUserContent(messages.getMessage("profile.emailUnavailable", null, locale));
		}
		jteOutput.writeContent("</span></div><div class=\"row\"><b>");
		jteOutput.setContext("b", null);
		jteOutput.writeUserContent(messages.getMessage("profile.givenName", null, locale));
		jteOutput.writeContent("</b><span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(user.givenName());
		jteOutput.writeContent("</span></div><div class=\"row\"><b>");
		jteOutput.setContext("b", null);
		jteOutput.writeUserContent(messages.getMessage("profile.familyName", null, locale));
		jteOutput.writeContent("</b><span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(user.familyName());
		jteOutput.writeContent("</span></div><div class=\"form-actions\"><a class=\"button\" href=\"/profile/edit\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("profile.edit", null, locale));
		jteOutput.writeContent("</a><a class=\"button secondary\" href=\"/profile/password\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("profile.changePassword", null, locale));
		jteOutput.writeContent("</a></div></section>\n    <section class=\"card\"><h2>");
		jteOutput.setContext("h2", null);
		jteOutput.writeUserContent(messages.getMessage("profile.roles", null, locale));
		jteOutput.writeContent("</h2>");
		if (roles.isEmpty()) {
			jteOutput.writeContent("<p class=\"muted\">—</p>");
		} else {
			jteOutput.writeContent(" ");
			for (String role : roles) {
				jteOutput.writeContent("<span class=\"pill\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(role);
				jteOutput.writeContent("</span>");
			}
			jteOutput.writeContent(" ");
		}
		jteOutput.writeContent("<h2>");
		jteOutput.setContext("h2", null);
		jteOutput.writeUserContent(messages.getMessage("profile.permissions", null, locale));
		jteOutput.writeContent("</h2>");
		if (permissions.isEmpty()) {
			jteOutput.writeContent("<p class=\"muted\">—</p>");
		} else {
			jteOutput.writeContent(" ");
			for (String permission : permissions) {
				jteOutput.writeContent("<span class=\"pill\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(permission);
				jteOutput.writeContent("</span>");
			}
			jteOutput.writeContent(" ");
		}
		jteOutput.writeContent("</section></div>\n    <section class=\"card section-card\"><h2>");
		jteOutput.setContext("h2", null);
		jteOutput.writeUserContent(messages.getMessage("profile.claims", null, locale));
		jteOutput.writeContent("</h2>");
		for (var entry : claims.entrySet()) {
			jteOutput.writeContent("<div class=\"row\"><b>");
			jteOutput.setContext("b", null);
			jteOutput.writeUserContent(entry.getKey());
			jteOutput.writeContent("</b><span>");
			jteOutput.setContext("span", null);
			jteOutput.writeUserContent(String.valueOf(entry.getValue()));
			jteOutput.writeContent("</span></div>");
		}
		jteOutput.writeContent("</section>\n</main><footer class=\"footer\">");
		jteOutput.setContext("footer", null);
		jteOutput.writeUserContent(messages.getMessage("common.footer", null, locale));
		jteOutput.writeContent("</footer></body></html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		org.springframework.context.MessageSource messages = (org.springframework.context.MessageSource)params.get("messages");
		java.util.Locale locale = (java.util.Locale)params.get("locale");
		kh.edu.istad.platform.iam.domain.dto.result.UserResult user = (kh.edu.istad.platform.iam.domain.dto.result.UserResult)params.get("user");
		java.util.Map<String,Object> claims = (java.util.Map<String,Object>)params.get("claims");
		java.util.List<String> roles = (java.util.List<String>)params.get("roles");
		java.util.List<String> permissions = (java.util.List<String>)params.get("permissions");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameter = (String)params.get("csrfParameter");
		render(jteOutput, jteHtmlInterceptor, messages, locale, user, claims, roles, permissions, csrfToken, csrfParameter);
	}
}
