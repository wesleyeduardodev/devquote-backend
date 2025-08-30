package br.com.devquote.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String TRACE_ID_KEY = "traceId";
    private static final String REQUEST_START_TIME = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Gerar trace ID único para correlação de logs
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);
        
        // Marcar tempo de início
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());

        // Log apenas para endpoints críticos ou erros
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        // Logar apenas operações críticas (POST, PUT, DELETE) ou endpoints sensíveis
        if (shouldLogRequest(method, uri)) {
            String userAgent = request.getHeader("User-Agent");
            String remoteAddr = getClientIpAddr(request);
            
            log.info("REQ {} {} from {} [{}]", 
                method, uri, remoteAddr, 
                userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "unknown"
            );
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();
            
            // Calcular tempo de execução
            Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
            long executionTime = startTime != null ? System.currentTimeMillis() - startTime : 0;

            // Log para operações críticas, erros ou requests lentos
            if (shouldLogResponse(method, uri, status, executionTime)) {
                if (status >= 400) {
                    // Log de erro com mais detalhes
                    log.error("ERR {} {} -> {} in {}ms", method, uri, status, executionTime);
                } else if (executionTime > 2000) {
                    // Log de performance (requests > 2s)
                    log.warn("SLOW {} {} -> {} in {}ms", method, uri, status, executionTime);
                } else if (shouldLogRequest(method, uri)) {
                    // Log de sucesso para operações críticas
                    log.info("OK {} {} -> {} in {}ms", method, uri, status, executionTime);
                }
            }

            // Log de exceções
            if (ex != null) {
                log.error("EXCEPTION {} {} -> {}: {}", method, uri, ex.getClass().getSimpleName(), ex.getMessage());
            }
        } finally {
            // Limpar MDC
            MDC.clear();
        }
    }

    /**
     * Decide se deve logar a requisição baseado no método e URI
     */
    private boolean shouldLogRequest(String method, String uri) {
        // Logar operações de escrita (CREATE, UPDATE, DELETE)
        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
            return true;
        }
        
        // Logar endpoints sensíveis
        if (uri.contains("/auth") || uri.contains("/login") || uri.contains("/oauth")) {
            return true;
        }

        // Não logar operações de leitura simples, health checks, etc.
        if (uri.contains("/health") || uri.contains("/actuator") || uri.contains("/favicon.ico")) {
            return false;
        }

        return false;
    }

    /**
     * Decide se deve logar a resposta
     */
    private boolean shouldLogResponse(String method, String uri, int status, long executionTime) {
        // Sempre logar erros
        if (status >= 400) {
            return true;
        }
        
        // Sempre logar requests lentos
        if (executionTime > 2000) {
            return true;
        }
        
        // Logar baseado na requisição
        return shouldLogRequest(method, uri);
    }

    /**
     * Extrai o IP real do cliente (considerando proxies)
     */
    private String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Se tem múltiplos IPs (proxy chain), pegar o primeiro
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}