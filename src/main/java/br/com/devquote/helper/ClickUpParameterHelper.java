package br.com.devquote.helper;

import br.com.devquote.configuration.IntegrationsProperties;
import br.com.devquote.service.SystemParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Config do ClickUp.
 *
 * Lê primeiro do system_parameter (banco, editável via /parameters):
 *   - CLICKUP_TOKEN
 *   - CLICKUP_INTEGRATION_ENABLED  ("true"/"false")
 *
 * Se ausente no banco, faz fallback pro {@link IntegrationsProperties}
 * (que lê de env var). Esse fallback existe pra transição — pode ser
 * removido depois que todos os tenants cadastrarem os 2 parâmetros
 * via UI.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClickUpParameterHelper {

    private final IntegrationsProperties integrationsProperties;
    private final SystemParameterService systemParameterService;

    public boolean isIntegrationEnabled() {
        Boolean fromDb = systemParameterService.getBoolean("CLICKUP_INTEGRATION_ENABLED", null);
        if (fromDb != null) return fromDb;
        return Boolean.TRUE.equals(integrationsProperties.getClickup().getEnabled());
    }

    public String getClickUpToken() {
        String fromDb = systemParameterService.getString("CLICKUP_TOKEN", null);
        if (fromDb != null && !fromDb.trim().isEmpty()) return fromDb;
        return integrationsProperties.getClickup().getToken();
    }
}
