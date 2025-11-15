package br.com.devquote.helper;

import br.com.devquote.dto.response.SystemParameterResponse;
import br.com.devquote.error.WhatsAppException;
import br.com.devquote.service.SystemParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WhatsAppParameterHelper {

    private final SystemParameterService systemParameterService;

    public String getBaseUrl() {
        return getParameterValue("base-url-whatsapp-api");
    }

    public String getMessageUrl() {
        return getParameterValue("url-mensagem-whatsapp-api");
    }

    public String getInstance() {
        return getParameterValue("instancia-whatsapp-api");
    }

    public String getApiKey() {
        return getParameterValue("api-key-whatsapp-api");
    }

    public String buildFullUrl() {
        return getBaseUrl() + getMessageUrl() + getInstance();
    }

    private String getParameterValue(String name) {
        try {
            SystemParameterResponse param = systemParameterService.findByName(name);
            if (param.getValue() == null || param.getValue().trim().isEmpty()) {
                throw new WhatsAppException("Parâmetro sem valor: " + name, "PARAMETER_EMPTY");
            }
            return param.getValue();
        } catch (Exception e) {
            throw new WhatsAppException("Parâmetro não encontrado: " + name, "PARAMETER_NOT_FOUND");
        }
    }
}
