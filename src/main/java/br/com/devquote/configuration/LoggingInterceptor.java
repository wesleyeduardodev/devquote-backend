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
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);

        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());

        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (shouldLogRequest(method, uri)) {
            String userAgent = request.getHeader("User-Agent");
            String remoteAddr = getClientIpAddr(request);
            
            log.debug("REQ {} {} from {} [{}]", 
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

            Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
            long executionTime = startTime != null ? System.currentTimeMillis() - startTime : 0;

            if (shouldLogResponse(method, uri, status, executionTime)) {
                if (status >= 400) {
                    log.error("ERR {} {} -> {} in {}ms", method, uri, status, executionTime);
                } else if (executionTime > 2000) {
                    log.warn("SLOW {} {} -> {} in {}ms", method, uri, status, executionTime);
                } else if (shouldLogRequest(method, uri)) {
                    log.debug("OK {} {} -> {} in {}ms", method, uri, status, executionTime);
                }
            }

            if (ex != null) {
                log.error("EXCEPTION {} {} -> {}: {}", method, uri, ex.getClass().getSimpleName(), ex.getMessage());
            }
        } finally {
            MDC.clear();
        }
    }

    private boolean shouldLogRequest(String method, String uri) {
        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
            return true;
        }

        if (uri.contains("/auth") || uri.contains("/login") || uri.contains("/oauth")) {
            return true;
        }

        if (uri.contains("/health") || uri.contains("/actuator") || uri.contains("/favicon.ico")) {
            return false;
        }

        return false;
    }

    private boolean shouldLogResponse(String method, String uri, int status, long executionTime) {
        if (status >= 400) {
            return true;
        }

        if (executionTime > 2000) {
            return true;
        }

        return shouldLogRequest(method, uri);
    }

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

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}