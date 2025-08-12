package br.com.devquote.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String description;

    @Column(name = "screen_path")
    private String screenPath; // Para controle de telas

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enum para permissões de telas
    public enum ScreenPermission {
        // Dashboard
        DASHBOARD_VIEW("dashboard:view", "Visualizar Dashboard", "/dashboard"),

        // Projetos
        PROJECTS_VIEW("projects:view", "Visualizar Projetos", "/projects"),
        PROJECTS_CREATE("projects:create", "Criar Projetos", "/projects/create"),
        PROJECTS_EDIT("projects:edit", "Editar Projetos", "/projects/edit"),
        PROJECTS_DELETE("projects:delete", "Deletar Projetos", null),

        // Tasks
        TASKS_VIEW("tasks:view", "Visualizar Tasks", "/tasks"),
        TASKS_CREATE("tasks:create", "Criar Tasks", "/tasks/create"),
        TASKS_EDIT("tasks:edit", "Editar Tasks", "/tasks/edit"),
        TASKS_DELETE("tasks:delete", "Deletar Tasks", null),
        TASKS_ASSIGN("tasks:assign", "Atribuir Tasks", null),

        // Orçamentos
        QUOTES_VIEW("quotes:view", "Visualizar Orçamentos", "/quotes"),
        QUOTES_CREATE("quotes:create", "Criar Orçamentos", "/quotes/create"),
        QUOTES_EDIT("quotes:edit", "Editar Orçamentos", "/quotes/edit"),
        QUOTES_APPROVE("quotes:approve", "Aprovar Orçamentos", null),

        // Entregas
        DELIVERIES_VIEW("deliveries:view", "Visualizar Entregas", "/deliveries"),
        DELIVERIES_MANAGE("deliveries:manage", "Gerenciar Entregas", "/deliveries/manage"),

        // Administrativo
        ADMIN_USERS("admin:users", "Gerenciar Usuários", "/admin/users"),
        ADMIN_ROLES("admin:roles", "Gerenciar Permissões", "/admin/roles"),
        ADMIN_SETTINGS("admin:settings", "Configurações", "/admin/settings");

        private final String name;
        private final String description;
        private final String screenPath;

        ScreenPermission(String name, String description, String screenPath) {
            this.name = name;
            this.description = description;
            this.screenPath = screenPath;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getScreenPath() {
            return screenPath;
        }
    }
}