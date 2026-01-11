package br.com.devquote.helper;

import br.com.devquote.service.SystemParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickUpParameterHelper {

    private static final String CLICKUP_INTEGRATION_ENABLED = "CLICKUP_INTEGRATION_ENABLED";
    private static final String CLICKUP_TOKEN = "CLICKUP_TOKEN";

    private final SystemParameterService systemParameterService;

    public boolean isIntegrationEnabled() {
        String enabled = systemParameterService.getValue(CLICKUP_INTEGRATION_ENABLED);
        return "true".equalsIgnoreCase(enabled);
    }

    public String getClickUpToken() {
        return systemParameterService.getValue(CLICKUP_TOKEN);
    }
}
