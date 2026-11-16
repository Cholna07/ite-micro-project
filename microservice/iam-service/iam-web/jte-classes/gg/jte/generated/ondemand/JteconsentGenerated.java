package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteconsentGenerated {
	public static final String JTE_NAME = "consent.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,8,8,8,8,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,11,11,11,11,11,11,11,11,11,13,13,13,14,14,14,14,14,14,15,15,15,15,15,15,15,15,15,16,16,16,16,16,16,16,16,16,16,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,17,18,18,18,18,18,18,20,20,20,21,0,0,0,1,2,3,4,5,6,7,0,0,0};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, org.springframework.context.MessageSource messages, java.util.Locale locale, String clientId, String clientName, String state, java.util.List<String> scopes, String csrfToken, String csrfParameter) {
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
		jteOutput.writeUserContent(messages.getMessage("consent.title", null, locale));
		jteOutput.writeContent("</title><link rel=\"stylesheet\" href=\"/css/iam.css\"></head><body>\n<main class=\"auth-layout\"><section class=\"card auth-card\">\n    <form method=\"get\" action=\"/oauth2/consent\" class=\"language\"><input type=\"hidden\" name=\"client_id\"");
		var __jte_html_attribute_1 = clientId;
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_1);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent("><input type=\"hidden\" name=\"state\"");
		var __jte_html_attribute_2 = state;
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_2);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">");
		for (String item : scopes) {
			jteOutput.writeContent("<input type=\"hidden\" name=\"scope\"");
			var __jte_html_attribute_3 = item;
			if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_3)) {
				jteOutput.writeContent(" value=\"");
				jteOutput.setContext("input", "value");
				jteOutput.writeUserContent(__jte_html_attribute_3);
				jteOutput.setContext("input", null);
				jteOutput.writeContent("\"");
			}
			jteOutput.writeContent(">");
		}
		jteOutput.writeContent("\n        <span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(messages.getMessage("common.language", null, locale));
		jteOutput.writeContent("</span><button class=\"secondary\" name=\"lang\" value=\"en\">");
		jteOutput.setContext("button", null);
		jteOutput.writeUserContent(messages.getMessage("common.english", null, locale));
		jteOutput.writeContent("</button><button class=\"secondary\" name=\"lang\" value=\"km\">");
		jteOutput.setContext("button", null);
		jteOutput.writeUserContent(messages.getMessage("common.khmer", null, locale));
		jteOutput.writeContent("</button>\n    </form>\n    <div class=\"auth-brand\"><span class=\"brand-mark\"><svg width=\"21\" height=\"21\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" aria-hidden=\"true\"><path d=\"M12 3 19 6v5c0 4.7-3 8-7 10-4-2-7-5.3-7-10V6l7-3Z\"/><path d=\"M12 8v4m0 4h.01\"/></svg></span>");
		jteOutput.setContext("div", null);
		jteOutput.writeUserContent(messages.getMessage("common.brand", null, locale));
		jteOutput.writeContent("</div>\n    <h1>");
		jteOutput.setContext("h1", null);
		jteOutput.writeUserContent(messages.getMessage("consent.title", null, locale));
		jteOutput.writeContent("</h1><p class=\"auth-intro\">");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(messages.getMessage("consent.request", new Object[]{clientName}, locale));
		jteOutput.writeContent("</p>\n    <div class=\"provider-card\"><div class=\"provider-name\"><span class=\"brand-mark\"><svg width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" aria-hidden=\"true\"><rect x=\"3\" y=\"4\" width=\"18\" height=\"16\" rx=\"2\"/><path d=\"M8 9h8M8 13h5\"/></svg></span><span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(clientName);
		jteOutput.writeContent("</span></div><span class=\"status\">");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(messages.getMessage("consent.clientId", null, locale));
		jteOutput.writeContent(": ");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(clientId);
		jteOutput.writeContent("</span></div>\n    <h2>");
		jteOutput.setContext("h2", null);
		jteOutput.writeUserContent(messages.getMessage("consent.scopes", null, locale));
		jteOutput.writeContent("</h2><ul class=\"scope-list\">");
		for (String item : scopes) {
			jteOutput.writeContent("<li>");
			jteOutput.setContext("li", null);
			jteOutput.writeUserContent(item);
			jteOutput.writeContent("</li>");
		}
		jteOutput.writeContent("</ul>\n    <form method=\"post\" action=\"/oauth2/authorize\"><input type=\"hidden\"");
		var __jte_html_attribute_4 = csrfParameter;
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_4)) {
			jteOutput.writeContent(" name=\"");
			jteOutput.setContext("input", "name");
			jteOutput.writeUserContent(__jte_html_attribute_4);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		var __jte_html_attribute_5 = csrfToken;
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_5)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_5);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent("><input type=\"hidden\" name=\"client_id\"");
		var __jte_html_attribute_6 = clientId;
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_6)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_6);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent("><input type=\"hidden\" name=\"state\"");
		var __jte_html_attribute_7 = state;
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_7)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_7);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">");
		for (String item : scopes) {
			jteOutput.writeContent("<input type=\"hidden\" name=\"scope\"");
			var __jte_html_attribute_8 = item;
			if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_8)) {
				jteOutput.writeContent(" value=\"");
				jteOutput.setContext("input", "value");
				jteOutput.writeUserContent(__jte_html_attribute_8);
				jteOutput.setContext("input", null);
				jteOutput.writeContent("\"");
			}
			jteOutput.writeContent(">");
		}
		jteOutput.writeContent("\n        <div class=\"form-actions\"><button type=\"submit\" class=\"full-width\" name=\"user_oauth_approval\" value=\"true\">");
		jteOutput.setContext("button", null);
		jteOutput.writeUserContent(messages.getMessage("consent.allow", null, locale));
		jteOutput.writeContent("</button><button type=\"submit\" class=\"secondary full-width\" name=\"user_oauth_approval\" value=\"false\">");
		jteOutput.setContext("button", null);
		jteOutput.writeUserContent(messages.getMessage("consent.cancel", null, locale));
		jteOutput.writeContent("</button></div>\n    </form>\n</section><footer class=\"footer\">");
		jteOutput.setContext("footer", null);
		jteOutput.writeUserContent(messages.getMessage("common.footer", null, locale));
		jteOutput.writeContent("</footer></main></body></html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		org.springframework.context.MessageSource messages = (org.springframework.context.MessageSource)params.get("messages");
		java.util.Locale locale = (java.util.Locale)params.get("locale");
		String clientId = (String)params.get("clientId");
		String clientName = (String)params.get("clientName");
		String state = (String)params.get("state");
		java.util.List<String> scopes = (java.util.List<String>)params.get("scopes");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameter = (String)params.get("csrfParameter");
		render(jteOutput, jteHtmlInterceptor, messages, locale, clientId, clientName, state, scopes, csrfToken, csrfParameter);
	}
}
