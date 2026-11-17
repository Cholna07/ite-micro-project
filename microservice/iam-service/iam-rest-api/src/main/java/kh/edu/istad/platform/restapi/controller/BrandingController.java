package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.domain.usecase.config.GetSystemConfigurationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BrandingController {
    private final GetSystemConfigurationUseCase getConfiguration;

    @GetMapping("/api/branding")
    public BrandingResponse getBranding() {
        return new BrandingResponse("ISTAD IAM", getConfiguration.execute().logoUrl());
    }

    public record BrandingResponse(String name, String logoUrl) { }
}
