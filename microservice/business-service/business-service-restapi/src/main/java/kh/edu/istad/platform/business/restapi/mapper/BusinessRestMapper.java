package kh.edu.istad.platform.business.restapi.mapper;

import kh.edu.istad.platform.business.domain.dto.RegisterBusinessCommand;
import kh.edu.istad.platform.business.domain.dto.RegisterBusinessResult;
import kh.edu.istad.platform.business.restapi.dto.BusinessResponse;
import kh.edu.istad.platform.business.restapi.dto.CreateBusinessRequest;

public class BusinessRestMapper {

    private BusinessRestMapper() {}

    public static RegisterBusinessCommand toCommand(
            CreateBusinessRequest request) {
        return new RegisterBusinessCommand(
                request.getLegalName(),
                request.getDisplayName(),
                request.getEmail(),
                request.getPhoneNumber()
        );
    }

    public static BusinessResponse toResponse(
            RegisterBusinessResult result) {
        return new BusinessResponse(
                result.businessId(),
                result.legalName(),
                result.displayName(),
                result.email(),
                result.phoneNumber(),
                result.active()
        );
    }
}