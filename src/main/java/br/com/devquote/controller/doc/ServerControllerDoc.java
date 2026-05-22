package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.ServerRequest;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.dto.response.ServerResponse;
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

@Tag(name = "Servers")
public interface ServerControllerDoc {

    @Operation(summary = "Lista servidores com paginação, ordenação e busca")
    @ApiResponse(responseCode = "200", description = "Lista paginada de servidores")
    ResponseEntity<PagedResponse<ServerResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String link,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam MultiValueMap<String, String> params
    );

    @Operation(summary = "Busca servidor por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servidor encontrado"),
            @ApiResponse(responseCode = "404", description = "Servidor não encontrado")
    })
    ResponseEntity<ServerResponse> getById(@Parameter(description = "Server id", required = true) Long id);

    @Operation(summary = "Cria um servidor")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Servidor criado")})
    ResponseEntity<ServerResponse> create(@Parameter(description = "Server payload", required = true) @Valid ServerRequest dto);

    @Operation(summary = "Atualiza um servidor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servidor atualizado"),
            @ApiResponse(responseCode = "404", description = "Servidor não encontrado")
    })
    ResponseEntity<ServerResponse> update(
            @Parameter(description = "Server id", required = true) Long id,
            @Parameter(description = "Server payload", required = true) @Valid ServerRequest dto);

    @Operation(summary = "Remove um servidor")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Servidor removido")})
    ResponseEntity<Void> delete(@Parameter(description = "Server id", required = true) Long id);

    @Operation(summary = "Remove múltiplos servidores")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Servidores removidos")})
    ResponseEntity<Void> deleteBulk(
            @RequestBody(
                    description = "Lista de IDs de servidores para remover",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))
            )
            List<Long> ids
    );
}
