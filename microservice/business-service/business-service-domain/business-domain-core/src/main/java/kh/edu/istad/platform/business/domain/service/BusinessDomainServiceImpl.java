package kh.edu.istad.platform.business.domain.service;

import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.exception.BusinessDomainException;

public class BusinessDomainServiceImpl
        implements BusinessDomainService {

    @Override
    public void validateActiveBusiness(Business business) {

        if (business == null) {
            throw new BusinessDomainException(
                    "Business cannot be null"
            );
        }

        if (!business.isActive()) {
            throw new BusinessDomainException(
                    "Inactive business cannot perform operations"
            );
        }
    }
}