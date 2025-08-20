package br.com.devquote.adapter;
import br.com.devquote.dto.request.ProjectRequest;
import br.com.devquote.dto.response.ProjectResponse;
import br.com.devquote.entity.Project;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProjectAdapter {

    public static ProjectResponse toResponseDTO(Project entity) {
        if (entity == null) {
            return null;
        }

        return ProjectResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .repositoryUrl(entity.getRepositoryUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Project toEntity(ProjectRequest dto) {
        if (dto == null) {
            return null;
        }

        return Project.builder()
                .name(dto.getName())
                .repositoryUrl(dto.getRepositoryUrl())
                .build();
    }

    public static void updateEntityFromDto(ProjectRequest dto, Project entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setRepositoryUrl(dto.getRepositoryUrl());
    }
}
