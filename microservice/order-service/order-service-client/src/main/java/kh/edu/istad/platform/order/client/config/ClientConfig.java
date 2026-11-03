package kh.edu.istad.platform.order.client.config;

import kh.edu.istad.platform.order.client.exception.DownStreamServiceException;
import kh.edu.istad.platform.order.client.httpclient.CustomerHttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;

import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(
        group = "customer-service",
        types = {CustomerHttpClient.class},
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT
)
public class ClientConfig {

    @Bean
    public ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2ClientFilter(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {

        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                        authorizedClientManager
                );

        oauth2.setDefaultClientRegistrationId("customer-client");

        return oauth2;
    }

    @Bean
    public WebClientHttpServiceGroupConfigurer webClientHttpServiceGroupConfigurer(
            ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2ClientFilter) {

        return groups -> groups
                .filterByName("customer-service")
                .forEachClient((group, clientBuilder) -> {

                    clientBuilder
                            .filter(oauth2ClientFilter)
                            .defaultHeader("User-Agent", "Web Client")
                            .defaultStatusHandler(
                                    HttpStatusCode::isError,
                                    clientResponse ->
                                            clientResponse.createException()
                                                    .map(ex ->
                                                            new DownStreamServiceException(
                                                                    "Customer-Service",
                                                                    ex.getMessage()
                                                            )
                                                    )
                            );
                });
    }
}