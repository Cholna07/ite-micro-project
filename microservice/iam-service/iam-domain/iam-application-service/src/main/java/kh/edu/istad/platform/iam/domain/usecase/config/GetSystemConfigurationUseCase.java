package kh.edu.istad.platform.iam.domain.usecase.config;

import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;
import kh.edu.istad.platform.iam.domain.port.out.SystemConfigurationRepository;
import org.springframework.stereotype.Component;

@Component
public class GetSystemConfigurationUseCase {
    private final SystemConfigurationRepository repository;
    public GetSystemConfigurationUseCase(SystemConfigurationRepository repository) { this.repository = repository; }
    public IamSystemConfiguration execute() { return repository.findCurrent(); }
}
