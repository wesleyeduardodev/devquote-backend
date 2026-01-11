package br.com.devquote.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
@Slf4j
public class GitHubConfig {

    @Bean
    public RestTemplate gitHubRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(30000);

        restTemplate.setRequestFactory(factory);

        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return response.getStatusCode().is4xxClientError()
                        || response.getStatusCode().is5xxServerError();
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                log.error("Erro na API GitHub - Status: {}", response.getStatusCode());
            }
        });

        return restTemplate;
    }
}
