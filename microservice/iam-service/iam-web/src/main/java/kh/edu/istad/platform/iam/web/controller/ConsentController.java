package kh.edu.istad.platform.iam.web.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ConsentController {
    private final RegisteredClientRepository clients;
    public ConsentController(RegisteredClientRepository clients) { this.clients = clients; }

    @GetMapping("/oauth2/consent")
    public String consent(@RequestParam(required = false, name = "client_id") String clientId,
                          @RequestParam(required = false, name = "state") String state,
                          @RequestParam(required = false, name = "scope") List<String> scope,
                          @RequestParam(required = false, name = "client_name") String clientName,
                          @RequestAttribute(name = "_csrf", required = false) CsrfToken csrf,
                          Model model) {
        model.addAttribute("clientId", clientId == null ? "" : clientId);
        model.addAttribute("state", state == null ? "" : state);
        model.addAttribute("scopes", scope == null ? List.of() : scope);
        RegisteredClient client = clientId == null ? null : clients.findByClientId(clientId);
        model.addAttribute("clientName", clientName != null && !clientName.isBlank() ? clientName
                : client == null ? clientId : client.getClientName());
        model.addAttribute("csrfToken", csrf == null ? "" : csrf.getToken());
        model.addAttribute("csrfParameter", csrf == null ? "_csrf" : csrf.getParameterName());
        return "consent";
    }
}
