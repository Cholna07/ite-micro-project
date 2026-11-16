package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteconnectedappsGenerated {
	public static final String JTE_NAME = "connected-apps.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,5,5,5,5,5,5,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,7,7,7,8,0,0,0,1,2,0,0,0};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, org.springframework.context.MessageSource messages, java.util.Locale locale, java.util.List<?> applications) {
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
		jteOutput.writeUserContent(messages.getMessage("profile.connectedApps", null, locale));
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
		jteOutput.writeContent("</a><a href=\"/profile/apps?lang=en\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.english", null, locale));
		jteOutput.writeContent("</a><a href=\"/profile/apps?lang=km\">");
		jteOutput.setContext("a", null);
		jteOutput.writeUserContent(messages.getMessage("common.khmer", null, locale));
		jteOutput.writeContent("</a></nav></header>\n<main class=\"page\"><div class=\"page-heading\"><div><h1>");
		jteOutput.setContext("h1", null);
		jteOutput.writeUserContent(messages.getMessage("profile.connectedApps", null, locale));
		jteOutput.writeContent("</h1><p class=\"muted\">");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(messages.getMessage("apps.description", null, locale));
		jteOutput.writeContent("</p></div></div>\n    <section class=\"card\">");
		if (applications.isEmpty()) {
			jteOutput.writeContent("<div class=\"empty-state\">");
			jteOutput.setContext("div", null);
			jteOutput.writeUserContent(messages.getMessage("apps.unsupported", null, locale));
			jteOutput.writeContent("</div>");
		} else {
			jteOutput.writeContent(" ");
			for (var app : applications) {
				jteOutput.writeContent("<div class=\"provider-card\"><div><strong>");
				jteOutput.setContext("strong", null);
				jteOutput.writeUserContent(String.valueOf(app));
				jteOutput.writeContent("</strong></div></div>");
			}
			jteOutput.writeContent(" ");
		}
		jteOutput.writeContent("</section>\n</main><footer class=\"footer\">");
		jteOutput.setContext("footer", null);
		jteOutput.writeUserContent(messages.getMessage("common.footer", null, locale));
		jteOutput.writeContent("</footer></body></html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		org.springframework.context.MessageSource messages = (org.springframework.context.MessageSource)params.get("messages");
		java.util.Locale locale = (java.util.Locale)params.get("locale");
		java.util.List<?> applications = (java.util.List<?>)params.get("applications");
		render(jteOutput, jteHtmlInterceptor, messages, locale, applications);
	}
}
