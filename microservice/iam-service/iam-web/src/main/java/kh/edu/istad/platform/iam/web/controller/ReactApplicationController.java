package kh.edu.istad.platform.iam.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the React application for browser routes so history navigation and refresh work. */
@Controller
public class ReactApplicationController {

    @GetMapping({
            "/account", "/account/**",
            "/users", "/users/**",
            "/roles", "/roles/**",
            "/permissions", "/permissions/**",
            "/clients", "/clients/**",
            "/admin", "/admin/**",
            "/audit-logs", "/system-config"
    })
    public String application() {
        return "forward:/frontend/index.html";
    }
}
