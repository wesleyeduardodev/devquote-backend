/*
package br.com.devquote.config;

import br.com.devquote.entity.Permission;
import br.com.devquote.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInitializer {

    private final PermissionRepository permissionRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Order(500) // Executa antes de outros listeners
    @Transactional
    public void initializePermissions() {
        log.info("=== INICIALIZANDO PERMISSÕES ===");

        try {
            // Lista de todas as permissões disponíveis no sistema
            List<PermissionData> permissions = Arrays.asList(
                // Dashboard
                new PermissionData("dashboard:view", "Visualizar Dashboard", "/dashboard"),

                // Usuários/Solicitantes
                new PermissionData("users:view", "Visualizar Usuários", "/requesters"),
                new PermissionData("users:create", "Criar Usuários", "/requesters/create"),
                new PermissionData("users:edit", "Editar Usuários", "/requesters/edit"),
                new PermissionData("users:delete", "Deletar Usuários", null),

                // Projetos
                new PermissionData("projects:view", "Visualizar Projetos", "/projects"),
                new PermissionData("projects:create", "Criar Projetos", "/projects/create"),
                new PermissionData("projects:edit", "Editar Projetos", "/projects/edit"),
                new PermissionData("projects:delete", "Deletar Projetos", null),

                // Tasks
                new PermissionData("tasks:view", "Visualizar Tarefas", "/tasks"),
                new PermissionData("tasks:create", "Criar Tarefas", "/tasks/create"),
                new PermissionData("tasks:edit", "Editar Tarefas", "/tasks/edit"),
                new PermissionData("tasks:delete", "Deletar Tarefas", null),
                new PermissionData("tasks:assign", "Atribuir Tarefas", null),

                // Orçamentos
                new PermissionData("quotes:view", "Visualizar Orçamentos", "/quotes"),
                new PermissionData("quotes:create", "Criar Orçamentos", "/quotes/create"),
                new PermissionData("quotes:edit", "Editar Orçamentos", "/quotes/edit"),
                new PermissionData("quotes:delete", "Deletar Orçamentos", null),
                new PermissionData("quotes:approve", "Aprovar Orçamentos", null),

                // Entregas
                new PermissionData("deliveries:view", "Visualizar Entregas", "/deliveries"),
                new PermissionData("deliveries:create", "Criar Entregas", "/deliveries/create"),
                new PermissionData("deliveries:edit", "Editar Entregas", "/deliveries/edit"),
                new PermissionData("deliveries:delete", "Deletar Entregas", null),
                new PermissionData("deliveries:manage", "Gerenciar Entregas", "/deliveries/manage"),

                // Faturamento
                new PermissionData("billing:view", "Visualizar Faturamento", "/billing"),
                new PermissionData("billing:manage", "Gerenciar Faturamento", "/billing/manage"),
                new PermissionData("billing:export", "Exportar Faturamento", null),

                // Administrativo
                new PermissionData("admin:users", "Gerenciar Usuários", "/admin/users"),
                new PermissionData("admin:profiles", "Gerenciar Perfis", "/profiles"),
                new PermissionData("admin:permissions", "Gerenciar Permissões", "/admin/permissions"),
                new PermissionData("admin:settings", "Configurações do Sistema", "/admin/settings"),

                // Relatórios
                new PermissionData("reports:view", "Visualizar Relatórios", "/reports"),
                new PermissionData("reports:export", "Exportar Relatórios", null)
            );

            // Criar ou atualizar cada permissão
            for (PermissionData permData : permissions) {
                createOrUpdatePermission(permData);
            }

            log.info("Permissões inicializadas com sucesso! Total: {}", permissions.size());

        } catch (Exception e) {
            log.error("Erro ao inicializar permissões", e);
        }
    }

    private void createOrUpdatePermission(PermissionData permData) {
        try {
            Permission permission = permissionRepository.findByName(permData.name)
                    .orElse(new Permission());

            permission.setName(permData.name);
            permission.setDescription(permData.description);
            permission.setScreenPath(permData.screenPath);

            permissionRepository.save(permission);
            log.debug("Permissão '{}' salva com sucesso", permData.name);

        } catch (Exception e) {
            log.error("Erro ao criar/atualizar permissão: {}", permData.name, e);
        }
    }

    // Classe interna para representar dados de permissão
    private static class PermissionData {
        String name;
        String description;
        String screenPath;

        PermissionData(String name, String description, String screenPath) {
            this.name = name;
            this.description = description;
            this.screenPath = screenPath;
        }
    }
}*/
