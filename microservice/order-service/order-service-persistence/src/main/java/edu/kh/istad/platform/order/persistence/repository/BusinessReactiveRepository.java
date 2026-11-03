package edu.kh.istad.platform.order.persistence.repository;

import edu.kh.istad.platform.order.persistence.entity.BusinessEntity;
import edu.kh.istad.platform.order.persistence.entity.BusinessEntityId;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface BusinessReactiveRepository extends R2dbcRepository<BusinessEntity, BusinessEntityId> {

}
