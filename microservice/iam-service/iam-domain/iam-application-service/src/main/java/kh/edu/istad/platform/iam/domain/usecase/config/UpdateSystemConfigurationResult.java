package kh.edu.istad.platform.iam.domain.usecase.config;

import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;

public record UpdateSystemConfigurationResult(IamSystemConfiguration configuration, String auditDetails) { }
