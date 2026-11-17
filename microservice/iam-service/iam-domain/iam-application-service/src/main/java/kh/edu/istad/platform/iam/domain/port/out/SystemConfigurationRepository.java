package kh.edu.istad.platform.iam.domain.port.out;

import kh.edu.istad.platform.iam.domain.entity.IamSystemConfiguration;

public interface SystemConfigurationRepository {
    IamSystemConfiguration findCurrent();
    void update(IamSystemConfiguration configuration, String updatedBy);
}
