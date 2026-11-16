package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteloginGenerated {
	public static final String JTE_NAME = "login.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,0,0,12,12,12,12,12,12,12,12,12,15,15,15,15,15,15,21,21,21,21,21,21,21,21,21,22,22,22,23,23,23,23,23,23,24,24,24,24,24,24,24,25,25,25,25,25,25,25,26,26,26,26,26,26,26,27,27,27,27,27,27,27,27,27,27,27,27,27,27,27,27,27,28,28,28,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,30,30,30,32,32,32,32,32,33,33,33,33,33,33,33,34,34,34,34,34,34,34,35,35,35,35,35,35,35,36,36,36,36,36,36,36,37,37,37,37,37,37,37,38,38,38,38,38,38,39,39,39,40,40,40,42,0,0,0,1,2,3,4,5,6,7,8,9,10,0,0,0};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, org.springframework.context.MessageSource messages, java.util.Locale locale, boolean error, boolean logout, boolean registered, boolean googleAvailable, boolean githubAvailable, boolean facebookAvailable, boolean telegramAvailable, String csrfToken, String csrfParameter) {
		jteOutput.writeContent("<!doctype html>\n<html");
		var __jte_html_attribute_0 = locale.getLanguage();
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
			jteOutput.writeContent(" lang=\"");
			jteOutput.setContext("html", "lang");
			jteOutput.writeUserContent(__jte_html_attribute_0);
			jteOutput.setContext("html", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">\n<head>\n    <meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n    <title>");
		jteOutput.setContext("title", null);
		jteOutput.writeUserContent(messages.getMessage("login.title", null, locale));
		jteOutput.writeContent(" · ");
		jteOutput.setContext("title", null);
		jteOutput.writeUserContent(messages.getMessage("common.brand", null, locale));
		jteOutput.writeContent("</title>\n    <link rel=\"stylesheet\" href=\"/css/iam.css\">\n    <script defer src=\"/js/iam.js\"></script>\n</head>\n<body>\n<main class=\"auth-layout\"><section class=\"card auth-card\">\n    <div class=\"language\"><span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(messages.getMessage("common.language", null, locale));
		jteOutput.writeContent("</span><a href=\"/login?lang=en\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.english", null, locale));
		jteOutput.writeContent("</a><a href=\"/login?lang=km\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.khmer", null, locale));
		jteOutput.writeContent("</a></div>\n    <div class=\"auth-brand\"><span class=\"brand-mark\"><svg width=\"21\" height=\"21\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" aria-hidden=\"true\"><path d=\"M12 3 19 6v5c0 4.7-3 8-7 10-4-2-7-5.3-7-10V6l7-3Z\"/><path d=\"m9 12 2 2 4-4\"/></svg></span>");
		jteOutput.setContext("div", null);
		jteOutput.writeUserContent(messages.getMessage("common.brand", null, locale));
		jteOutput.writeContent("</div>\n    <h1>");
		jteOutput.setContext("h1", null);
		jteOutput.writeUserContent(messages.getMessage("login.title", null, locale));
		jteOutput.writeContent("</h1><p class=\"auth-intro\">");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(messages.getMessage("login.subtitle", null, locale));
		jteOutput.writeContent("</p>\n    ");
		if (error) {
			jteOutput.writeContent("<div class=\"alert error\" role=\"alert\">");
			jteOutput.setContext("div", null);
			jteOutput.writeUserContent(messages.getMessage("login.error", null, locale));
			jteOutput.writeContent("</div>");
		}
		jteOutput.writeContent("\n    ");
		if (logout) {
			jteOutput.writeContent("<div class=\"alert success\" role=\"status\">");
			jteOutput.setContext("div", null);
			jteOutput.writeUserContent(messages.getMessage("login.logoutSuccess", null, locale));
			jteOutput.writeContent("</div>");
		}
		jteOutput.writeContent("\n    ");
		if (registered) {
			jteOutput.writeContent("<div class=\"alert success\" role=\"status\">");
			jteOutput.setContext("div", null);
			jteOutput.writeUserContent(messages.getMessage("register.success", null, locale));
			jteOutput.writeContent("</div>");
		}
		jteOutput.writeContent("\n    <form method=\"post\" action=\"/login\"><input type=\"hidden\"");
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
		jteOutput.writeUserContent(messages.getMessage("login.username", null, locale));
		jteOutput.writeContent("</label><input id=\"username\" name=\"username\" autocomplete=\"username\" required autofocus></div>\n        <div class=\"field\"><label for=\"password\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("login.password", null, locale));
		jteOutput.writeContent("</label><div class=\"input-wrap\"><input id=\"password\" name=\"password\" type=\"password\" autocomplete=\"current-password\" required><button type=\"button\" class=\"toggle-password\" data-password-toggle");
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
		jteOutput.writeContent(" aria-pressed=\"false\"><svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" aria-hidden=\"true\"><path d=\"M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z\"/><circle cx=\"12\" cy=\"12\" r=\"3\"/></svg></button></div></div>\n        <button class=\"full-width\" type=\"submit\">");
		jteOutput.setContext("button", null);
		jteOutput.writeUserContent(messages.getMessage("login.submit", null, locale));
		jteOutput.writeContent("</button>\n    </form>\n    ");
		if (googleAvailable || githubAvailable || facebookAvailable || telegramAvailable) {
			jteOutput.writeContent("<div class=\"divider\">");
			jteOutput.setContext("div", null);
			jteOutput.writeUserContent(messages.getMessage("login.continueWith", null, locale));
			jteOutput.writeContent("</div><div class=\"social\">\n        ");
			if (googleAvailable) {
				jteOutput.writeContent("<a class=\"button secondary\" href=\"/oauth2/authorization/google\"><span class=\"provider-icon\" aria-hidden=\"true\">G</span>");
				jteOutput.setContext("a", null);
				jteOutput.writeUserContent(messages.getMessage("login.google", null, locale));
				jteOutput.writeContent("</a>");
			}
			jteOutput.writeContent("\n        ");
			if (githubAvailable) {
				jteOutput.writeContent("<a class=\"button secondary\" href=\"/oauth2/authorization/github\"><span class=\"provider-icon\" aria-hidden=\"true\">GH</span>");
				jteOutput.setContext("a", null);
				jteOutput.writeUserContent(messages.getMessage("login.github", null, locale));
				jteOutput.writeContent("</a>");
			}
			jteOutput.writeContent("\n        ");
			if (facebookAvailable) {
				jteOutput.writeContent("<a class=\"button secondary\" href=\"/oauth2/authorization/facebook\"><span class=\"provider-icon\" aria-hidden=\"true\">f</span>");
				jteOutput.setContext("a", null);
				jteOutput.writeUserContent(messages.getMessage("login.facebook", null, locale));
				jteOutput.writeContent("</a>");
			}
			jteOutput.writeContent("\n        ");
			if (telegramAvailable) {
				jteOutput.writeContent("<a class=\"button secondary\" href=\"/oauth2/authorization/telegram\"><span class=\"provider-icon\" aria-hidden=\"true\">↗</span>");
				jteOutput.setContext("a", null);
				jteOutput.writeUserContent(messages.getMessage("login.telegram", null, locale));
				jteOutput.writeContent("</a>");
			}
			jteOutput.writeContent("\n    </div>");
		} else {
			jteOutput.writeContent("<p class=\"muted\">");
			jteOutput.setContext("p", null);
			jteOutput.writeUserContent(messages.getMessage("login.socialUnavailable", null, locale));
			jteOutput.writeContent("</p>");
		}
		jteOutput.writeContent("\n    <p class=\"muted\">");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(messages.getMessage("login.noAccount", null, locale));
		jteOutput.writeContent(" <a href=\"/register\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("login.register", null, locale));
		jteOutput.writeContent("</a></p>\n    <p class=\"muted\"><a href=\"/login\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("login.forgotPassword", null, locale));
		jteOutput.writeContent("</a></p>\n</section><footer class=\"footer\">");
		jteOutput.setContext("footer", null);
		jteOutput.writeUserContent(messages.getMessage("common.footer", null, locale));
		jteOutput.writeContent("</footer></main>\n</body></html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		org.springframework.context.MessageSource messages = (org.springframework.context.MessageSource)params.get("messages");
		java.util.Locale locale = (java.util.Locale)params.get("locale");
		boolean error = (boolean)params.get("error");
		boolean logout = (boolean)params.get("logout");
		boolean registered = (boolean)params.get("registered");
		boolean googleAvailable = (boolean)params.get("googleAvailable");
		boolean githubAvailable = (boolean)params.get("githubAvailable");
		boolean facebookAvailable = (boolean)params.get("facebookAvailable");
		boolean telegramAvailable = (boolean)params.get("telegramAvailable");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameter = (String)params.get("csrfParameter");
		render(jteOutput, jteHtmlInterceptor, messages, locale, error, logout, registered, googleAvailable, githubAvailable, facebookAvailable, telegramAvailable, csrfToken, csrfParameter);
	}
}
