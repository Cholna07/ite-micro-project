
package kh.edu.istad.platform.customer.persistence.config;

import kh.edu.istad.platform.customer.persistence.convert.CustomerIdReadConverter;
import kh.edu.istad.platform.customer.persistence.convert.CustomerIdWriteConverter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.UUID;

@Configuration
@EnableReactiveMongoAuditing(
        auditorAwareRef = "auditorAwareConfig",
        modifyOnCreate = false
)
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return MongoCustomConversions.create(adapter -> {
            adapter.registerConverter(new CustomerIdReadConverter());
            adapter.registerConverter(new CustomerIdWriteConverter());

            adapter.registerConverter(new UuidToStringConverter());
            adapter.registerConverter(new StringToUuidConverter());
        });
    }

    @WritingConverter
    public static class UuidToStringConverter
            implements Converter<UUID, String> {

        @Override
        public String convert(UUID source) {
            return source == null ? null : source.toString();
        }
    }

    @ReadingConverter
    public static class StringToUuidConverter
            implements Converter<String, UUID> {

        @Override
        public UUID convert(String source) {
            return source == null ? null : UUID.fromString(source);
        }
    }
}
