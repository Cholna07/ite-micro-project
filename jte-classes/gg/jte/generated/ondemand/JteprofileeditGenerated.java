package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteprofileeditGenerated {
	public static final String JTE_NAME = "profile-edit.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,0,0,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,9,9,9,9,9,9,10,10,10,10,10,10,10,11,11,11,11,11,11,11,11,11,11,11,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,13,13,13,13,13,13,13,13,13,13,13,13,14,14,14,14,14,14,14,14,14,14,14,14,15,15,15,15,15,15,15,15,15,15,15,15,16,16,16,16,16,16,16,16,16,16,16,16,17,17,17,17,17,17,17,17,17,17,17,17,18,18,18,18,18,18,18,18,18,18,18,18,19,19,19,19,19,19,20,20,20,21,0,0,0,1,2,3,4,5,6,0,0,0};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, org.springframework.context.MessageSource messages, java.util.Locale locale, kh.edu.istad.platform.iam.domain.dto.result.UserResult user, java.util.List<String> errors, boolean saved, String csrfToken, String csrfParameter) {
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
		jteOutput.writeUserContent(messages.getMessage("profile.edit", null, locale));
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
		jteOutput.writeContent("</a><a href=\"/profile/edit?lang=en\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.english", null, locale));
		jteOutput.writeContent("</a><a href=\"/profile/edit?lang=km\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.khmer", null, locale));
		jteOutput.writeContent("</a></nav></header>\n<main class=\"page\"><div class=\"page-heading\"><div><h1>");
		jteOutput.setContext("h1", null);
		jteOutput.writeUserContent(messages.getMessage("profile.edit", null, locale));
		jteOutput.writeContent("</h1><p class=\"muted\">");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(messages.getMessage("profile.accountIdentifiersReadOnly", null, locale));
		jteOutput.writeContent("</p></div></div>\n    ");
		if (saved) {
			jteOutput.writeContent("<div class=\"alert success\" role=\"status\">");
			jteOutput.setContext("div", null);
			jteOutput.writeUserContent(messages.getMessage("profile.saved", null, locale));
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
		jteOutput.writeContent("\n    <section class=\"card\"><form method=\"post\" action=\"/profile/edit\"><input type=\"hidden\"");
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
		jteOutput.writeContent("><div class=\"grid\">\n        <div class=\"field\"><label for=\"givenName\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("profile.givenName", null, locale));
		jteOutput.writeContent("</label><input id=\"givenName\" name=\"givenName\"");
		var __jte_html_attribute_3 = user.givenName();
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_3)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_3);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(" autocomplete=\"given-name\"></div>\n        <div class=\"field\"><label for=\"familyName\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("profile.familyName", null, locale));
		jteOutput.writeContent("</label><input id=\"familyName\" name=\"familyName\"");
		var __jte_html_attribute_4 = user.familyName();
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_4)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_4);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(" autocomplete=\"family-name\"></div>\n        <div class=\"field\"><label for=\"middleName\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("profile.middleName", null, locale));
		jteOutput.writeContent("</label><input id=\"middleName\" name=\"middleName\"");
		var __jte_html_attribute_5 = user.middleName();
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_5)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_5);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent("></div>\n        <div class=\"field\"><label for=\"nickname\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("profile.nickname", null, locale));
		jteOutput.writeContent("</label><input id=\"nickname\" name=\"nickname\"");
		var __jte_html_attribute_6 = user.nickname();
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_6)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_6);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent("></div>\n        <div class=\"field\"><label for=\"gender\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("profile.gender", null, locale));
		jteOutput.writeContent("</label><input id=\"gender\" name=\"gender\"");
		var __jte_html_attribute_7 = user.gender();
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_7)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_7);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent("></div>\n        <div class=\"field\"><label for=\"phoneNumber\">");
		jteOutput.setContext("label", null);
		jteOutput.writeUserContent(messages.getMessage("profile.phone", null, locale));
		jteOutput.writeContent("</label><input id=\"phoneNumber\" name=\"phoneNumber\"");
		var __jte_html_attribute_8 = user.phoneNumber();
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_8)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_8);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(" autocomplete=\"tel\"></div>\n    </div><div class=\"form-actions\"><button type=\"submit\">");
		jteOutput.setContext("button", null);
		jteOutput.writeUserContent(messages.getMessage("common.save", null, locale));
		jteOutput.writeContent("</button><a class=\"button secondary\" href=\"/\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.cancel", null, locale));
		jteOutput.writeContent("</a></div></form></section>\n</main><footer class=\"footer\">");
		jteOutput.setContext("footer", null);
		jteOutput.writeUserContent(messages.getMessage("common.footer", null, locale));
		jteOutput.writeContent("</footer></body></html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		org.springframework.context.MessageSource messages = (org.springframework.context.MessageSource)params.get("messages");
		java.util.Locale locale = (java.util.Locale)params.get("locale");
		kh.edu.istad.platform.iam.domain.dto.result.UserResult user = (kh.edu.istad.platform.iam.domain.dto.result.UserResult)params.get("user");
		java.util.List<String> errors = (java.util.List<String>)params.get("errors");
		boolean saved = (boolean)params.get("saved");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameter = (String)params.get("csrfParameter");
		render(jteOutput, jteHtmlInterceptor, messages, locale, user, errors, saved, csrfToken, csrfParameter);
	}
}
