package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.ModuleRequest;
import br.com.devquote.dto.response.ModuleResponse;
import br.com.devquote.dto.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Modules")
public interface ModuleControllerDoc {

    @Operation(summary = "Lista módulos com paginação, ordenação e busca")
    @ApiResponse(responseCode = "200", description = "Lista paginada de módulos")
    ResponseEntity<PagedResponse<ModuleResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam MultiValueMap<String, String> params
    );

    @Operation(summary = "Busca módulo por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Módulo encontrado"),
            @ApiResponse(responseCode = "404", description = "Módulo não encontrado")
    })
    ResponseEntity<ModuleResponse> getById(@Parameter(description = "Module id", required = true) Long id);

    @Operation(summary = "Cria um módulo")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Módulo criado")})
    ResponseEntity<ModuleResponse> create(@Parameter(description = "Module payload", required = true) @Valid ModuleRequest dto);

    @Operation(summary = "Atualiza um módulo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Módulo atualizado"),
            @ApiResponse(responseCode = "404", description = "Módulo não encontrado")
    })
    ResponseEntity<ModuleResponse> update(
            @Parameter(description = "Module id", required = true) Long id,
            @Parameter(description = "Module payload", required = true) @Valid ModuleRequest dto);

    @Operation(summary = "Remove um módulo")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Módulo removido")})
    ResponseEntity<Void> delete(@Parameter(description = "Module id", required = true) Long id);

    @Operation(summary = "Remove múltiplos módulos")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Módulos removidos")})
    ResponseEntity<Void> deleteBulk(
            @RequestBody(
                    description = "Lista de IDs de módulos para remover",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))
            )
            List<Long> ids
    );
}
