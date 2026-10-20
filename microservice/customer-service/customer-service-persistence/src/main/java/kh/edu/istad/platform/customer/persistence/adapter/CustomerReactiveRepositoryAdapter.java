package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.CustomerFilter;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.persistence.document.CustomerDocument;
import kh.edu.istad.platform.customer.persistence.mapper.CustomerPersistenceMapper;
import kh.edu.istad.platform.customer.persistence.repository.CustomerMongoRepository;
import kh.edu.istad.platform.customer.persistence.specification.CustomerMongoFilter;
import kh.edu.istad.platform.customer.domain.dto.CustomerStatistics;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import reactor.core.publisher.Flux;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CustomerReactiveRepositoryAdapter implements CustomerRepository {

    private final CustomerMongoRepository customerMongoRepository;
    private final CustomerPersistenceMapper customerPersistenceMapper;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final CustomerMongoFilter customerMongoFilter;

    @Override
    public Mono<Customer> save(Customer customer) {
        String customerId = customer.getId().value().toString();

        return customerMongoRepository.findById(customerId)
                .flatMap(existingDocument -> {
                    CustomerDocument updatedDocument =
                            customerPersistenceMapper.toDocument(customer);

                    // Preserve creation audit fields during updates.
                    updatedDocument.setCreatedAt(existingDocument.getCreatedAt());
                    updatedDocument.setCreatedBy(existingDocument.getCreatedBy());
                    updatedDocument.setNewDocument(false);

                    return customerMongoRepository.save(updatedDocument);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    CustomerDocument newDocument =
                            customerPersistenceMapper.toDocument(customer);

                    newDocument.setNewDocument(true);

                    return customerMongoRepository.save(newDocument);
                }))
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Customer> findById(CustomerId customerId) {
        return customerMongoRepository
                .findById(customerId.value().toString())
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public Flux<Customer> findAll() {
        return customerMongoRepository.findAll()
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsById(CustomerId customerId) {
        return customerMongoRepository
                .existsById(customerId.value().toString());
    }

    @Override
    public Mono<Void> deleteById(CustomerId customerId) {
        return customerMongoRepository
                .deleteById(customerId.value().toString());
    }

    @Override
    public Flux<Customer> findAll(
            CustomerFilter filter,
            int page,
            int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            return Flux.error(
                    new IllegalArgumentException(
                            "Page must be non-negative and size must be between 1 and 100"
                    )
            );
        }

        Query query = customerMongoFilter.build(filter);

        query.with(PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "username")
        ));

        return reactiveMongoTemplate
                .find(query, CustomerDocument.class)
                .map(customerPersistenceMapper::toDomain);
    }
    @Override
    public Flux<CustomerStatistics> getStatistics() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("status", "segmentId")
                        .count().as("totalCustomers"),

                Aggregation.project("totalCustomers")
                        .and("_id.status").as("status")
                        .and("_id.segmentId").as("segmentId")
                        .andExclude("_id")
        );

        return reactiveMongoTemplate.aggregate(
                aggregation,
                CustomerDocument.class,
                CustomerStatistics.class
        );
    }
}
