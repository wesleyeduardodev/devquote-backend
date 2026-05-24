package br.com.devquote.service;

import br.com.devquote.dto.request.ClickUpSetupSaveRequest;
import br.com.devquote.dto.response.ClickUpSetupFieldsResponse;
import br.com.devquote.dto.response.ClickUpSetupItemResponse;
import br.com.devquote.dto.response.ClickUpSetupUserResponse;

import java.util.List;

/**
 * Service que dirige o wizard de setup do ClickUp.
 * Usa o TOKEN passado pelo caller (não o do banco) — isso permite validar e
 * navegar pela hierarquia (workspace → space → list → fields) ANTES de persistir.
 */
public interface ClickUpSetupService {

    ClickUpSetupUserResponse validateToken(String token);

    List<ClickUpSetupItemResponse> listTeams(String token);

    List<ClickUpSetupItemResponse> listSpaces(String token, String teamId);

    /** Combina lists folderless + lists dentro de folders (ambas viram itens). */
    List<ClickUpSetupItemResponse> listLists(String token, String spaceId);

    /**
     * Lista as listas que foram compartilhadas com o user nesse workspace
     * (Shared with me). Cobre o caso onde a lista vive num space privado
     * que o user só tem acesso pontual à list.
     */
    List<ClickUpSetupItemResponse> listSharedLists(String token, String teamId);

    /** Lista os custom fields + sugestões auto-detect baseadas no nome e no username. */
    ClickUpSetupFieldsResponse listFields(String token, String listId);

    /** Persiste os 5 parâmetros no system_parameter (cria ou atualiza). */
    void save(ClickUpSetupSaveRequest request);
}
