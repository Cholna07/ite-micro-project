package kh.edu.istad.platform.customer.persistence.convert;

import kh.edu.istad.platform.customer.domain.valueobject.Email;
import org.springframework.core.convert.converter.Converter;

public class EmailWriteConverter implements Converter<Email, String> {

    @Override
    public String convert(Email source) {
        return source.value().toString();
    }
}
