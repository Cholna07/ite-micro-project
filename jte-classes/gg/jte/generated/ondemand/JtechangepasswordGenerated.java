package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtechangepasswordGenerated {
	public static final String JTE_NAME = "change-password.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,0,0,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,8,8,8,8,8,8,9,9,9,9,9,9,9,10,10,10,10,10,10,10,10,10,10,10,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,15,15,15,15,15,15,17,17,17,18,0,0,0,1,2,3,4,5,0,0,0};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, org.springframework.context.MessageSource messages, java.util.Locale locale, java.util.List<String> errors, boolean saved, String csrfToken, String csrfParameter) {
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
		jteOutput.writeUserContent(messages.getMessage("profile.changePassword", null, locale));
		jteOutput.writeContent(" · ");
		jteOutput.setContext("title", null);
		jteOutput.writeUserContent(messages.getMessage("common.brand", null, locale));
		jteOutput.writeContent("</title><link rel=\"stylesheet\" href=\"/css/iam.css\"><script defer src=\"/js/iam.js\"></script></head><body>\n<header class=\"topbar\"><a class=\"brand\" href=\"/\">");
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
		jteOutput.writeContent("</a><a href=\"/profile/password?lang=en\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.english", null, locale));
		jteOutput.writeContent("</a><a href=\"/profile/password?lang=km\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.khmer", null, locale));
		jteOutput.writeContent("</a></nav></header>\n<main class=\"page\"><div class=\"page-heading\"><div><h1>");
		jteOutput.setContext("h1", null);
		jteOutput.writeUserContent(messages.getMessage("profile.changePassword", null, locale));
		jteOutput.writeContent("</h1><p class=\"muted\">");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(messages.getMessage("password.securityHint", null, locale));
		jteOutput.writeContent("</p></div></div>\n    ");
		if (saved) {
			jteOutput.writeContent("<div class=\"alert success\" role=\"status\">");
			jteOutput.setContext("div", null);
			jteOutput.writeUserContent(messages.getMessage("password.saved", null, locale));
			jteOutput.writeContent("</div>");
		}
		jteOutput.writeContent("\n    ");
		if (!errors.isEmpty()) {
			jteOutput.writeContent("<div class=\"alert error\" role=\"alert\">");
			for (String error : errors) {
				jteOutput.writeContent("<div>");
				jteOutput.setContext("div", null);
				jteOutput.writeUserContent(error);
				jteOutput.writeContent("</div>");
			}
			jteOutput.writeContent("</div>");
		}
		jteOutput.writeContent("\n    <section class=\"card\"><form method=\"post\" action=\"/profile/password\"><input type=\"hidden\"");
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
		jteOutput.writeContent(">\n        <div class=\"field\"><label for=\"currentPassword\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("password.current", null, locale));
		jteOutput.writeContent("</label><div class=\"input-wrap\"><input id=\"currentPassword\" type=\"password\" name=\"currentPassword\" autocomplete=\"current-password\" required><button type=\"button\" class=\"toggle-password\" data-password-toggle");
		var __jte_html_attribute_3 = messages.getMessage("common.showPassword", null, locale);
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_3)) {
			jteOutput.writeContent(" data-show-label=\"");
			jteOutput.setContext("button", "data-show-label");
			jteOutput.writeUserContent(__jte_html_attribute_3);
			jteOutput.setContext("button", null);
			jteOutput.writeContent("\"");
		}
		var __jte_html_attribute_4 = messages.getMessage("common.hidePassword", null, locale);
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_4)) {
			jteOutput.writeContent(" data-hide-label=\"");
			jteOutput.setContext("button", "data-hide-label");
			jteOutput.writeUserContent(__jte_html_attribute_4);
			jteOutput.setContext("button", null);
			jteOutput.writeContent("\"");
		}
		var __jte_html_attribute_5 = messages.getMessage("common.showPassword", null, locale);
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_5)) {
			jteOutput.writeContent(" aria-label=\"");
			jteOutput.setContext("button", "aria-label");
			jteOutput.writeUserContent(__jte_html_attribute_5);
			jteOutput.setContext("button", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(" aria-pressed=\"false\"><svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" aria-hidden=\"true\"><path d=\"M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z\"/><circle cx=\"12\" cy=\"12\" r=\"3\"/></svg></button></div></div>\n        <div class=\"field\"><label for=\"newPassword\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("password.new", null, locale));
		jteOutput.writeContent("</label><div class=\"input-wrap\"><input id=\"newPassword\" type=\"password\" name=\"newPassword\" autocomplete=\"new-password\" required><button type=\"button\" class=\"toggle-password\" data-password-toggle");
		var __jte_html_attribute_6 = messages.getMessage("common.showPassword", null, locale);
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_6)) {
			jteOutput.writeContent(" data-show-label=\"");
			jteOutput.setContext("button", "data-show-label");
			jteOutput.writeUserContent(__jte_html_attribute_6);
			jteOutput.setContext("button", null);
			jteOutput.writeContent("\"");
		}
		var __jte_html_attribute_7 = messages.getMessage("common.hidePassword", null, locale);
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_7)) {
			jteOutput.writeContent(" data-hide-label=\"");
			jteOutput.setContext("button", "data-hide-label");
			jteOutput.writeUserContent(__jte_html_attribute_7);
			jteOutput.setContext("button", null);
			jteOutput.writeContent("\"");
		}
		var __jte_html_attribute_8 = messages.getMessage("common.showPassword", null, locale);
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_8)) {
			jteOutput.writeContent(" aria-label=\"");
			jteOutput.setContext("button", "aria-label");
			jteOutput.writeUserContent(__jte_html_attribute_8);
			jteOutput.setContext("button", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(" aria-pressed=\"false\"><svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" aria-hidden=\"true\"><path d=\"M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z\"/><circle cx=\"12\" cy=\"12\" r=\"3\"/></svg></button></div></div>\n        <div class=\"field\"><label for=\"confirmPassword\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("password.confirm", null, locale));
		jteOutput.writeContent("</label><div class=\"input-wrap\"><input id=\"confirmPassword\" type=\"password\" name=\"confirmPassword\" autocomplete=\"new-password\" required><button type=\"button\" class=\"toggle-password\" data-password-toggle");
		var __jte_html_attribute_9 = messages.getMessage("common.showPassword", null, locale);
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_9)) {
			jteOutput.writeContent(" data-show-label=\"");
			jteOutput.setContext("button", "data-show-label");
			jteOutput.writeUserContent(__jte_html_attribute_9);
			jteOutput.setContext("button", null);
			jteOutput.writeContent("\"");
		}
		var __jte_html_attribute_10 = messages.getMessage("common.hidePassword", null, locale);
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_10)) {
			jteOutput.writeContent(" data-hide-label=\"");
			jteOutput.setContext("button", "data-hide-label");
			jteOutput.writeUserContent(__jte_html_attribute_10);
			jteOutput.setContext("button", null);
			jteOutput.writeContent("\"");
		}
		var __jte_html_attribute_11 = messages.getMessage("common.showPassword", null, locale);
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_11)) {
			jteOutput.writeContent(" aria-label=\"");
			jteOutput.setContext("button", "aria-label");
			jteOutput.writeUserContent(__jte_html_attribute_11);
			jteOutput.setContext("button", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(" aria-pressed=\"false\"><svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" aria-hidden=\"true\"><path d=\"M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z\"/><circle cx=\"12\" cy=\"12\" r=\"3\"/></svg></button></div></div>\n        <div class=\"form-actions\"><button type=\"submit\">");
		jteOutput.setContext("button", null);
		jteOutput.writeUserContent(messages.getMessage("common.save", null, locale));
		jteOutput.writeContent("</button><a class=\"button secondary\" href=\"/\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.cancel", null, locale));
		jteOutput.writeContent("</a></div>\n    </form></section>\n</main><footer class=\"footer\">");
		jteOutput.setContext("footer", null);
		jteOutput.writeUserContent(messages.getMessage("common.footer", null, locale));
		jteOutput.writeContent("</footer></body></html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		org.springframework.context.MessageSource messages = (org.springframework.context.MessageSource)params.get("messages");
		java.util.Locale locale = (java.util.Locale)params.get("locale");
		java.util.List<String> errors = (java.util.List<String>)params.get("errors");
		boolean saved = (boolean)params.get("saved");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameter = (String)params.get("csrfParameter");
		render(jteOutput, jteHtmlInterceptor, messages, locale, errors, saved, csrfToken, csrfParameter);
	}
}
