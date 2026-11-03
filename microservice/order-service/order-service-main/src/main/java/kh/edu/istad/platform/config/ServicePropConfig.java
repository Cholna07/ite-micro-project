package kh.edu.istad.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "service")
public class ServicePropConfig {
    private  String message;
    private String env;
    private String version;
    private String baseUrl;


}
