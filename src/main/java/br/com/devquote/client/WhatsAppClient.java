package br.com.devquote.client;

import br.com.devquote.dto.request.WhatsAppMessageRequest;
import br.com.devquote.error.WhatsAppException;
import br.com.devquote.helper.WhatsAppParameterHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class WhatsAppClient {

    private final RestTemplate whatsAppRestTemplate;
    private final WhatsAppParameterHelper parameterHelper;

    public void sendTextMessage(String destinatario, String text) {

        String url = parameterHelper.buildFullUrl();
        String apiKey = parameterHelper.getApiKey();

        log.info("Enviando mensagem WhatsApp para: {} - URL: {}", destinatario, url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);

        WhatsAppMessageRequest request = WhatsAppMessageRequest.builder()
                .number(destinatario)
                .text(text)
                .build();

        HttpEntity<WhatsAppMessageRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = whatsAppRestTemplate.postForEntity(
                    url,
                    entity,
                    String.class
            );

            log.info("Resposta WhatsApp API - Status: {} - Body: {}",
                    response.getStatusCode(),
                    response.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro HTTP ao enviar mensagem WhatsApp - Status: {} - Body: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new WhatsAppException("Erro ao enviar mensagem: " + e.getStatusCode(), "WHATSAPP_HTTP_ERROR");

        } catch (Exception e) {
            log.error("Erro inesperado ao enviar mensagem WhatsApp: {}", e.getMessage(), e);
            throw new WhatsAppException("Erro inesperado ao enviar mensagem", "WHATSAPP_UNEXPECTED_ERROR");
        }
    }
}
