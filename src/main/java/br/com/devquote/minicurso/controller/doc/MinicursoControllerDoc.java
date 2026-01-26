package br.com.devquote.minicurso.controller.doc;

import br.com.devquote.minicurso.dto.request.ConfiguracaoEventoRequest;
import br.com.devquote.minicurso.dto.request.InscricaoRequest;
import br.com.devquote.minicurso.dto.request.ItemModuloRequest;
import br.com.devquote.minicurso.dto.request.ModuloEventoRequest;
import br.com.devquote.minicurso.dto.response.ConfiguracaoEventoResponse;
import br.com.devquote.minicurso.dto.response.InscricaoResponse;
import br.com.devquote.minicurso.dto.response.ItemModuloResponse;
import br.com.devquote.minicurso.dto.response.ModuloEventoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Tag(name = "Minicurso", description = "Endpoints para gerenciamento de inscricoes do minicurso de IA")
public interface MinicursoControllerDoc {

    @Operation(summary = "Cadastrar nova inscricao", description = "Endpoint publico para cadastrar inscricao no minicurso")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscricao realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "409", description = "Email ja cadastrado")
    })
    ResponseEntity<InscricaoResponse> criarInscricao(@Valid InscricaoRequest request);

    @Operation(summary = "Verificar se email ja esta inscrito", description = "Endpoint publico para verificar disponibilidade de email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificacao realizada")
    })
    ResponseEntity<Map<String, Boolean>> verificarEmail(
            @Parameter(description = "Email para verificar", required = true) String email);

    @Operation(summary = "Obter total de inscritos", description = "Endpoint publico para obter quantidade de inscritos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado")
    })
    ResponseEntity<Map<String, Long>> contarInscritos();

    @Operation(summary = "Obter configuracao do evento", description = "Endpoint publico para obter dados do evento e modulos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuracao retornada")
    })
    ResponseEntity<ConfiguracaoEventoResponse> obterEvento();

    @Operation(summary = "Listar todas inscricoes", description = "Endpoint admin para listar todas as inscricoes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de inscricoes"),
            @ApiResponse(responseCode = "401", description = "Nao autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    ResponseEntity<List<InscricaoResponse>> listarInscricoes();

    @Operation(summary = "Buscar inscricao por ID", description = "Endpoint admin para buscar inscricao especifica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscricao encontrada"),
            @ApiResponse(responseCode = "404", description = "Inscricao nao encontrada")
    })
    ResponseEntity<InscricaoResponse> buscarInscricao(
            @Parameter(description = "ID da inscricao", required = true) Long id);

    @Operation(summary = "Excluir inscricao", description = "Endpoint admin para excluir inscricao")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Inscricao excluida"),
            @ApiResponse(responseCode = "404", description = "Inscricao nao encontrada")
    })
    ResponseEntity<Void> excluirInscricao(
            @Parameter(description = "ID da inscricao", required = true) Long id);

    @Operation(summary = "Exportar inscricoes para Excel", description = "Endpoint admin para exportar planilha de inscricoes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo Excel gerado")
    })
    ResponseEntity<byte[]> exportarInscricoes();

    @Operation(summary = "Exportar inscricoes para PDF", description = "Endpoint admin para exportar relatorio PDF de inscricoes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo PDF gerado")
    })
    ResponseEntity<byte[]> exportarInscricoesPdf();

    @Operation(summary = "Atualizar configuracao do evento", description = "Endpoint admin para atualizar dados do evento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuracao atualizada")
    })
    ResponseEntity<ConfiguracaoEventoResponse> atualizarEvento(@Valid ConfiguracaoEventoRequest request);

    @Operation(summary = "Criar modulo", description = "Endpoint admin para criar novo modulo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Modulo criado")
    })
    ResponseEntity<ModuloEventoResponse> criarModulo(@Valid ModuloEventoRequest request);

    @Operation(summary = "Atualizar modulo", description = "Endpoint admin para atualizar modulo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Modulo atualizado"),
            @ApiResponse(responseCode = "404", description = "Modulo nao encontrado")
    })
    ResponseEntity<ModuloEventoResponse> atualizarModulo(
            @Parameter(description = "ID do modulo", required = true) Long id,
            @Valid ModuloEventoRequest request);

    @Operation(summary = "Excluir modulo", description = "Endpoint admin para excluir modulo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Modulo excluido"),
            @ApiResponse(responseCode = "404", description = "Modulo nao encontrado")
    })
    ResponseEntity<Void> excluirModulo(
            @Parameter(description = "ID do modulo", required = true) Long id);

    @Operation(summary = "Criar item de modulo", description = "Endpoint admin para criar item dentro de um modulo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item criado")
    })
    ResponseEntity<ItemModuloResponse> criarItem(
            @Parameter(description = "ID do modulo", required = true) Long moduloId,
            @Valid ItemModuloRequest request);

    @Operation(summary = "Atualizar item", description = "Endpoint admin para atualizar item de modulo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado"),
            @ApiResponse(responseCode = "404", description = "Item nao encontrado")
    })
    ResponseEntity<ItemModuloResponse> atualizarItem(
            @Parameter(description = "ID do item", required = true) Long id,
            @Valid ItemModuloRequest request);

    @Operation(summary = "Excluir item", description = "Endpoint admin para excluir item de modulo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item excluido"),
            @ApiResponse(responseCode = "404", description = "Item nao encontrado")
    })
    ResponseEntity<Void> excluirItem(
            @Parameter(description = "ID do item", required = true) Long id);
}
