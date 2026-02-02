package br.com.devquote.helper;

import br.com.devquote.configuration.IntegrationsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickUpParameterHelper {

    private final IntegrationsProperties integrationsProperties;

    public boolean isIntegrationEnabled() {
        return Boolean.TRUE.equals(integrationsProperties.getClickup().getEnabled());
    }

    public String getClickUpToken() {
        return integrationsProperties.getClickup().getToken();
    }
}
