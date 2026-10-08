package kh.edu.istad.platform.customer.persistence.repository;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerId, UUID> {
}
