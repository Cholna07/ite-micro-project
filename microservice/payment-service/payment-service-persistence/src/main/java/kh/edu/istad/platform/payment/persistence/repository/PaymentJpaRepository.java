package kh.edu.istad.platform.payment.persistence.repository;

import kh.edu.istad.platform.payment.persistence.entity.PaymentPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentJpaRepository
        extends JpaRepository<PaymentPersistenceEntity, UUID> {
}
