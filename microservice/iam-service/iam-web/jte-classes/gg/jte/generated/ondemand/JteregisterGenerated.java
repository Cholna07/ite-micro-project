package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteregisterGenerated {
	public static final String JTE_NAME = "register.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,0,0,6,6,6,6,6,6,6,6,6,7,7,7,7,7,7,9,9,9,9,9,9,9,9,9,10,10,10,11,11,11,12,12,12,12,12,12,12,12,12,12,12,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,14,14,14,15,15,15,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,18,18,18,19,19,19,19,19,19,20,20,20,21,0,0,0,1,2,3,4,0,0,0};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, org.springframework.context.MessageSource messages, java.util.Locale locale, java.util.List<String> errors, String csrfToken, String csrfParameter) {
		jteOutput.writeContent("<!doctype html>\n<html");
		var __jte_html_attribute_0 = locale.getLanguage();
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
			jteOutput.writeContent(" lang=\"");
			jteOutput.setContext("html", "lang");
			jteOutput.writeUserContent(__jte_html_attribute_0);
			jteOutput.setContext("html", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent("><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n<title>");
		jteOutput.setContext("title", null);
		jteOutput.writeUserContent(messages.getMessage("register.title", null, locale));
		jteOutput.writeContent(" · ");
		jteOutput.setContext("title", null);
		jteOutput.writeUserContent(messages.getMessage("common.brand", null, locale));
		jteOutput.writeContent("</title><link rel=\"stylesheet\" href=\"/css/iam.css\"><script defer src=\"/js/iam.js\"></script></head><body>\n<main class=\"auth-layout\"><section class=\"card auth-card\">\n    <div class=\"language\"><span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(messages.getMessage("common.language", null, locale));
		jteOutput.writeContent("</span><a href=\"/register?lang=en\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.english", null, locale));
		jteOutput.writeContent("</a><a href=\"/register?lang=km\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.khmer", null, locale));
		jteOutput.writeContent("</a></div>\n    <div class=\"auth-brand\"><span class=\"brand-mark\"><svg width=\"21\" height=\"21\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" aria-hidden=\"true\"><path d=\"M12 3 19 6v5c0 4.7-3 8-7 10-4-2-7-5.3-7-10V6l7-3Z\"/></svg></span>");
		jteOutput.setContext("div", null);
		jteOutput.writeUserContent(messages.getMessage("common.brand", null, locale));
		jteOutput.writeContent("</div>\n    <h1>");
		jteOutput.setContext("h1", null);
		jteOutput.writeUserContent(messages.getMessage("register.title", null, locale));
		jteOutput.writeContent("</h1>\n    ");
		if (!errors.isEmpty()) {
			jteOutput.writeContent("<div class=\"alert error\" role=\"alert\"><ul class=\"list\">");
			for (String error : errors) {
				jteOutput.writeContent("<li>");
				jteOutput.setContext("li", null);
				jteOutput.writeUserContent(error);
				jteOutput.writeContent("</li>");
			}
			jteOutput.writeContent("</ul></div>");
		}
		jteOutput.writeContent("\n    <form method=\"post\" action=\"/register\"><input type=\"hidden\"");
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
		jteOutput.writeContent(">\n        <div class=\"field\"><label for=\"username\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("register.username", null, locale));
		jteOutput.writeContent("</label><input id=\"username\" name=\"username\" autocomplete=\"username\" required maxlength=\"100\"></div>\n        <div class=\"field\"><label for=\"email\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("register.email", null, locale));
		jteOutput.writeContent("</label><input id=\"email\" name=\"email\" type=\"email\" autocomplete=\"email\" required></div>\n        <div class=\"field\"><label for=\"password\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("register.password", null, locale));
		jteOutput.writeContent("</label><div class=\"input-wrap\"><input id=\"password\" name=\"password\" type=\"password\" autocomplete=\"new-password\" required><button type=\"button\" class=\"toggle-password\" data-password-toggle");
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
		jteOutput.writeContent(" aria-pressed=\"false\"><svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" aria-hidden=\"true\"><path d=\"M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z\"/><circle cx=\"12\" cy=\"12\" r=\"3\"/></svg></button></div></div>\n        <div class=\"field\"><label for=\"confirmPassword\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("register.confirmPassword", null, locale));
		jteOutput.writeContent("</label><div class=\"input-wrap\"><input id=\"confirmPassword\" name=\"confirmPassword\" type=\"password\" autocomplete=\"new-password\" required><button type=\"button\" class=\"toggle-password\" data-password-toggle");
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
		jteOutput.writeContent(" aria-pressed=\"false\"><svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" aria-hidden=\"true\"><path d=\"M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z\"/><circle cx=\"12\" cy=\"12\" r=\"3\"/></svg></button></div></div>\n        <button class=\"full-width\" type=\"submit\">");
		jteOutput.setContext("button", null);
		jteOutput.writeUserContent(messages.getMessage("register.submit", null, locale));
		jteOutput.writeContent("</button>\n    </form><p class=\"muted\">");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(messages.getMessage("register.haveAccount", null, locale));
		jteOutput.writeContent(" <a href=\"/login\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.backToLogin", null, locale));
		jteOutput.writeContent("</a></p>\n</section><footer class=\"footer\">");
		jteOutput.setContext("footer", null);
		jteOutput.writeUserContent(messages.getMessage("common.footer", null, locale));
		jteOutput.writeContent("</footer></main></body></html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		org.springframework.context.MessageSource messages = (org.springframework.context.MessageSource)params.get("messages");
		java.util.Locale locale = (java.util.Locale)params.get("locale");
		java.util.List<String> errors = (java.util.List<String>)params.get("errors");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameter = (String)params.get("csrfParameter");
		render(jteOutput, jteHtmlInterceptor, messages, locale, errors, csrfToken, csrfParameter);
	}
}
