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
        return getParameterValue("BASE_URL_WHATSAPP_API");
    }

    public String getMessageUrl() {
        return getParameterValue("URL_MENSAGEM_WHATSAPP_API");
    }

    public String getInstance() {
        return getParameterValue("INSTANCIA_WHATSAPP_API");
    }

    public String getApiKey() {
        return getParameterValue("API_KEY_WHATSAPP_API");
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
