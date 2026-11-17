package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.domain.usecase.config.GetSystemConfigurationUseCase;
import kh.edu.istad.platform.iam.domain.usecase.config.UpdateSystemConfigurationUseCase;
import kh.edu.istad.platform.restapi.dto.request.UpdateSystemConfigurationRequest;
import kh.edu.istad.platform.restapi.dto.response.SystemConfigurationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/system-config")
@RequiredArgsConstructor
public class AdminSystemConfigurationController {
    private final GetSystemConfigurationUseCase getConfiguration;
    private final UpdateSystemConfigurationUseCase updateConfiguration;

    @GetMapping
    public ResponseEntity<SystemConfigurationResponse> get() {
        return ResponseEntity.ok(SystemConfigurationResponse.from(getConfiguration.execute()));
    }

    @PutMapping
    public ResponseEntity<SystemConfigurationResponse> update(
            @Valid @RequestBody UpdateSystemConfigurationRequest request) {
        var result = updateConfiguration.execute(request.toConfiguration());
        return ResponseEntity.ok(SystemConfigurationResponse.from(result.configuration()));
    }
}
