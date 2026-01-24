package br.com.devquote.minicurso.controller;

import br.com.devquote.minicurso.controller.doc.MinicursoControllerDoc;
import br.com.devquote.minicurso.dto.request.ConfiguracaoEventoRequest;
import br.com.devquote.minicurso.dto.request.InscricaoRequest;
import br.com.devquote.minicurso.dto.request.InstrutorRequest;
import br.com.devquote.minicurso.dto.request.ItemModuloRequest;
import br.com.devquote.minicurso.dto.request.ModuloEventoRequest;
import br.com.devquote.minicurso.dto.response.ConfiguracaoEventoResponse;
import br.com.devquote.minicurso.dto.response.InscricaoResponse;
import br.com.devquote.minicurso.dto.response.InstrutorResponse;
import br.com.devquote.minicurso.dto.response.ItemModuloResponse;
import br.com.devquote.minicurso.dto.response.ModuloEventoResponse;
import br.com.devquote.minicurso.service.ConfiguracaoEventoService;
import br.com.devquote.minicurso.service.InscricaoMinicursoService;
import br.com.devquote.minicurso.service.InstrutorMinicursoService;
import br.com.devquote.minicurso.service.ItemModuloService;
import br.com.devquote.minicurso.service.ModuloEventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/minicurso")
@Validated
@RequiredArgsConstructor
public class MinicursoController implements MinicursoControllerDoc {

    private final InscricaoMinicursoService inscricaoService;
    private final ConfiguracaoEventoService configuracaoEventoService;
    private final ModuloEventoService moduloEventoService;
    private final ItemModuloService itemModuloService;
    private final InstrutorMinicursoService instrutorService;

    @Override
    @PostMapping("/inscricao")
    public ResponseEntity<InscricaoResponse> criarInscricao(@Valid @RequestBody InscricaoRequest request) {
        InscricaoResponse response = inscricaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/inscricao/check")
    public ResponseEntity<Map<String, Boolean>> verificarEmail(@RequestParam String email) {
        boolean existe = inscricaoService.verificarEmailExiste(email);
        return ResponseEntity.ok(Map.of("exists", existe));
    }

    @Override
    @GetMapping("/inscricoes/count")
    public ResponseEntity<Map<String, Long>> contarInscritos() {
        long total = inscricaoService.contarInscritos();
        return ResponseEntity.ok(Map.of("total", total));
    }

    @Override
    @GetMapping("/evento")
    public ResponseEntity<ConfiguracaoEventoResponse> obterEvento() {
        ConfiguracaoEventoResponse response = configuracaoEventoService.obterConfiguracao();
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/inscricoes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InscricaoResponse>> listarInscricoes() {
        List<InscricaoResponse> inscricoes = inscricaoService.listarTodas();
        return ResponseEntity.ok(inscricoes);
    }

    @Override
    @GetMapping("/inscricoes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InscricaoResponse> buscarInscricao(@PathVariable Long id) {
        InscricaoResponse response = inscricaoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/inscricoes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluirInscricao(@PathVariable Long id) {
        inscricaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/inscricoes/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportarInscricoes() {
        try {
            byte[] excelBytes = inscricaoService.exportarExcel();

            String filename = "inscricoes_minicurso_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    @PutMapping("/evento")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracaoEventoResponse> atualizarEvento(@Valid @RequestBody ConfiguracaoEventoRequest request) {
        ConfiguracaoEventoResponse response = configuracaoEventoService.atualizar(request);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/modulos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuloEventoResponse> criarModulo(@Valid @RequestBody ModuloEventoRequest request) {
        Long eventoId = configuracaoEventoService.obterEventoIdAtual();
        if (eventoId == null) {
            return ResponseEntity.badRequest().build();
        }
        ModuloEventoResponse response = moduloEventoService.criar(eventoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PutMapping("/modulos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuloEventoResponse> atualizarModulo(@PathVariable Long id, @Valid @RequestBody ModuloEventoRequest request) {
        ModuloEventoResponse response = moduloEventoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/modulos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluirModulo(@PathVariable Long id) {
        moduloEventoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/modulos/{moduloId}/itens")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemModuloResponse> criarItem(@PathVariable Long moduloId, @Valid @RequestBody ItemModuloRequest request) {
        ItemModuloResponse response = itemModuloService.criar(moduloId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PutMapping("/itens/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemModuloResponse> atualizarItem(@PathVariable Long id, @Valid @RequestBody ItemModuloRequest request) {
        ItemModuloResponse response = itemModuloService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/itens/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluirItem(@PathVariable Long id) {
        itemModuloService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ENDPOINTS DE INSTRUTORES ====================

    @GetMapping("/instrutores")
    public ResponseEntity<List<InstrutorResponse>> listarInstrutoresAtivos() {
        List<InstrutorResponse> instrutores = instrutorService.listarAtivos();
        return ResponseEntity.ok(instrutores);
    }

    @GetMapping("/admin/instrutores")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InstrutorResponse>> listarTodosInstrutores() {
        List<InstrutorResponse> instrutores = instrutorService.listarTodos();
        return ResponseEntity.ok(instrutores);
    }

    @GetMapping("/admin/instrutores/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstrutorResponse> buscarInstrutor(@PathVariable Long id) {
        InstrutorResponse response = instrutorService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/instrutores")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstrutorResponse> criarInstrutor(@Valid @RequestBody InstrutorRequest request) {
        InstrutorResponse response = instrutorService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/admin/instrutores/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstrutorResponse> atualizarInstrutor(@PathVariable Long id, @Valid @RequestBody InstrutorRequest request) {
        InstrutorResponse response = instrutorService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/instrutores/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluirInstrutor(@PathVariable Long id) {
        instrutorService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/admin/instrutores/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstrutorResponse> uploadFotoInstrutor(@PathVariable Long id, @RequestParam("foto") MultipartFile foto) {
        InstrutorResponse response = instrutorService.atualizarFoto(id, foto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/instrutores/{id}/foto")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstrutorResponse> removerFotoInstrutor(@PathVariable Long id) {
        InstrutorResponse response = instrutorService.removerFoto(id);
        return ResponseEntity.ok(response);
    }
}
