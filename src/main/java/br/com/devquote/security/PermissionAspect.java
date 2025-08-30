package br.com.devquote.security;

import br.com.devquote.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    @Before("@annotation(requiresPermission)")
    public void checkPermission(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        // Obter ID do usuário (assumindo que está no nome do usuário ou principal)
        Long userId = getCurrentUserId(authentication);
        
        if (userId == null) {
            throw new AccessDeniedException("Não foi possível identificar o usuário");
        }

        // Verificar se é admin (se permitido)
        if (requiresPermission.allowAdmin() && permissionService.isAdmin(userId)) {
            log.debug("ACCESS GRANTED admin userId={} resource={} operation={}", 
                userId, requiresPermission.resource(), requiresPermission.operation());
            return;
        }

        // Verificar permissão específica
        boolean hasPermission = permissionService.hasPermission(
            userId, 
            requiresPermission.resource(), 
            requiresPermission.operation()
        );

        if (!hasPermission) {
            log.warn("ACCESS DENIED userId={} resource={} operation={} method={}", 
                userId, requiresPermission.resource(), requiresPermission.operation(), 
                joinPoint.getSignature().getName());
            throw new AccessDeniedException(
                String.format("Usuário não tem permissão para %s em %s", 
                    requiresPermission.operation(), requiresPermission.resource())
            );
        }

        log.debug("ACCESS GRANTED userId={} resource={} operation={}", 
            userId, requiresPermission.resource(), requiresPermission.operation());
    }

    @Before("@annotation(requiresProfile)")
    public void checkProfile(JoinPoint joinPoint, RequiresProfile requiresProfile) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        Long userId = getCurrentUserId(authentication);
        
        if (userId == null) {
            throw new AccessDeniedException("Não foi possível identificar o usuário");
        }

        List<String> requiredProfiles = Arrays.asList(requiresProfile.value());
        boolean hasRequiredProfile;

        if (requiresProfile.requireAll()) {
            // Usuário deve ter TODOS os perfis
            hasRequiredProfile = requiredProfiles.stream()
                .allMatch(profile -> permissionService.hasAnyProfile(userId, List.of(profile)));
        } else {
            // Usuário deve ter PELO MENOS UM dos perfis
            hasRequiredProfile = permissionService.hasAnyProfile(userId, requiredProfiles);
        }

        if (!hasRequiredProfile) {
            log.warn("ACCESS DENIED PROFILE userId={} requiredProfiles={} requireAll={} method={}", 
                userId, requiredProfiles, requiresProfile.requireAll(), joinPoint.getSignature().getName());
            throw new AccessDeniedException(
                String.format("Usuário não possui o(s) perfil(is) necessário(s): %s", 
                    String.join(", ", requiredProfiles))
            );
        }

        log.debug("ACCESS GRANTED PROFILE userId={} profiles={}", userId, requiredProfiles);
    }

    private Long getCurrentUserId(Authentication authentication) {
        try {
            // Tenta obter do nome (se for um ID)
            String name = authentication.getName();
            return Long.parseLong(name);
        } catch (NumberFormatException e) {
            // Se não conseguir converter, verifica se há um objeto User no principal
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                // Implementar lógica para extrair ID do User se necessário
                return null;
            }
            
            // Última tentativa: buscar em claims do JWT (se estiver usando)
            // Implementar se necessário
            
            log.warn("Não foi possível extrair userId do authentication: {}", authentication);
            return null;
        }
    }
}