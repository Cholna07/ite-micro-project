package kh.edu.istad.platform.restapi.mapper;

import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.restapi.dto.response.PermissionResponse;
import org.springframework.stereotype.Component;

@Component
public class PermissionRestMapper {

    public PermissionResponse toResponse(Permission permission) {
        return new PermissionResponse(
                permission.getUuid(),
                permission.getName().value(),
                permission.getResource().value(),
                permission.getAction().value(),
                permission.getDescription(),
                permission.isActive(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}
