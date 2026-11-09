package kh.edu.istad.platform.restapi.mapper;

import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.restapi.dto.response.RoleResponse;
import org.springframework.stereotype.Component;

@Component
public class RoleRestMapper {

    public RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getUuid(),
                role.getName().value(),
                role.getDescription(),
                role.isActive(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
