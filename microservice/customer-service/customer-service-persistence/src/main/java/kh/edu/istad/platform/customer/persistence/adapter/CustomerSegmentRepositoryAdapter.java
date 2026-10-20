
package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.platform.customer.domain.entity.CustomerSegment;
import kh.edu.istad.platform.customer.domain.port.out.CustomerSegmentRepository;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerSegmentId;
import kh.edu.istad.platform.customer.persistence.mapper.CustomerSegmentPersistenceMapper;
import kh.edu.istad.platform.customer.persistence.repository.CustomerSegmentMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CustomerSegmentRepositoryAdapter implements CustomerSegmentRepository {

    private final CustomerSegmentMongoRepository customerSegmentMongoRepository;
    private final CustomerSegmentPersistenceMapper customerSegmentPersistenceMapper;

    @Override
    public Mono<CustomerSegment> save(CustomerSegment segment) {
        return customerSegmentMongoRepository
                .save(customerSegmentPersistenceMapper.toDocument(segment))
                .map(customerSegmentPersistenceMapper::toDomain);
    }

    @Override
    public Mono<CustomerSegment> findById(CustomerSegmentId id) {
        return customerSegmentMongoRepository
                .findById(id.value().toString())
                .map(customerSegmentPersistenceMapper::toDomain);
    }

    @Override
    public Flux<CustomerSegment> findAll() {
        return customerSegmentMongoRepository.findAll()
                .map(customerSegmentPersistenceMapper::toDomain);
    }


    @Override
    public Mono<Void> deleteById(CustomerSegmentId id) {
        return customerSegmentMongoRepository
                .deleteById(id.value().toString());
    }
}
