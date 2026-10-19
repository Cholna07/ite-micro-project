package kh.edu.istad.platform.business.persistence.adapter;


import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.port.out.BusinessRepository;
import kh.edu.istad.platform.business.persistence.entity.BusinessDocument;
import kh.edu.istad.platform.business.persistence.mapper.BusinessMapper;
import kh.edu.istad.platform.business.persistence.repository.BusinessMongoRepository;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BusinessRepositoryAdapter implements BusinessRepository {

    private final BusinessMongoRepository mongoRepository;

    public BusinessRepositoryAdapter(
            BusinessMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Mono<Business> save(Business business) {
        BusinessDocument document = BusinessMapper.toDocument(business);

        return mongoRepository.save(document)
                .map(BusinessMapper::toDomain);
    }
}