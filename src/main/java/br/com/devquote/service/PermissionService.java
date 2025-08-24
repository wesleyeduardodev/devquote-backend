package br.com.devquote.service;

import br.com.devquote.dto.response.UserPermissionResponse;
import br.com.devquote.entity.FieldPermissionType;

import java.util.List;

public interface PermissionService {

    /**
     * Verifica se o usuário tem permissão para realizar uma operação em um recurso
     */
    boolean hasPermission(Long userId, String resourceCode, String operationCode);

    /**
     * Verifica se o usuário tem permissão para editar um campo específico
     */
    boolean canEditField(Long userId, String resourceCode, String fieldName);

    /**
     * Obtém o tipo de permissão do usuário para um campo específico
     */
    FieldPermissionType getFieldPermission(Long userId, String resourceCode, String fieldName);

    /**
     * Obtém todas as permissões de um usuário
     */
    UserPermissionResponse getUserPermissions(Long userId);

    /**
     * Verifica se o usuário tem pelo menos um dos perfis especificados
     */
    boolean hasAnyProfile(Long userId, List<String> profileCodes);

    /**
     * Verifica se o usuário é administrador
     */
    boolean isAdmin(Long userId);

    /**
     * Inicializa dados básicos do sistema de permissões
     */
    void initializeDefaultData();
}