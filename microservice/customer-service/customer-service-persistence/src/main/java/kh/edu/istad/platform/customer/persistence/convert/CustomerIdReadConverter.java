package kh.edu.istad.platform.customer.persistence.convert;

import org.springframework.core.convert.converter.Converter;
import kh.edu.istad.common.domain.valueobject.CustomerId;
import org.springframework.data.convert.ReadingConverter;

import java.util.UUID;


@ReadingConverter
public class CustomerIdReadConverter implements Converter<String, CustomerId> {
    @Override
    public CustomerId convert(String source) {
        return new CustomerId(UUID.fromString(source));
    }
}
