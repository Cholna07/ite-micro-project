
package kh.edu.istad.platform.business.domain.usecase;

import kh.edu.istad.platform.business.domain.dto.RegisterBusinessCommand;
import kh.edu.istad.platform.business.domain.dto.RegisterBusinessResult;
import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.port.out.BusinessRepository;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RegisterBusinessUseCase {

    private final BusinessRepository businessRepository;

    public RegisterBusinessUseCase(
            BusinessRepository businessRepository
    ) {
        this.businessRepository = businessRepository;
    }

    public Mono<RegisterBusinessResult> registerBusiness(
            RegisterBusinessCommand command
    ) {
        return Mono.fromCallable(() -> Business.register(
                        command.legalName(),
                        command.displayName(),
                        command.email(),
                        command.phoneNumber()
                ))
                .flatMap(businessRepository::save)
                .map(business -> new RegisterBusinessResult(
                        business.getBusinessId().value().toString(),
                        business.getBusinessName().legalName(),
                        business.getBusinessName().displayName(),
                        business.getBusinessContact().email(),
                        business.getBusinessContact().phoneNumber(),
                        business.isActive()
                ));
    }
}
