package br.com.devquote.service.impl;

import br.com.devquote.client.WhatsAppClient;
import br.com.devquote.error.BusinessException;
import br.com.devquote.service.WhatsAppService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    private final WhatsAppClient whatsAppClient;

    @Override
    public void sendMessage(String destinatario, String text) {

        if (destinatario == null || destinatario.trim().isEmpty()) {
            throw new BusinessException("Destinatário é obrigatório", "DESTINATARIO_REQUIRED");
        }

        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException("Texto da mensagem é obrigatório", "TEXT_REQUIRED");
        }

        log.debug("Enviando mensagem WhatsApp - Destinatário: {}, Tamanho texto: {}",
                destinatario, text.length());

        whatsAppClient.sendTextMessage(destinatario, text);
    }
}
