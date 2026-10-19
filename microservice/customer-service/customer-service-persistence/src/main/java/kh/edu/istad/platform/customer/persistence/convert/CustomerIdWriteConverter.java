package kh.edu.istad.platform.customer.persistence.convert;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;


@WritingConverter
public class CustomerIdWriteConverter implements Converter<CustomerId, String> {
    @Override
    public String convert(CustomerId source) {
        return source.id().toString();
    }
}