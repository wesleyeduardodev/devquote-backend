package br.com.devquote.minicurso.service;

import br.com.devquote.error.BusinessException;
import br.com.devquote.error.ResourceNotFoundException;
import br.com.devquote.minicurso.adapter.InstrutorMinicursoAdapter;
import br.com.devquote.minicurso.dto.request.InstrutorRequest;
import br.com.devquote.minicurso.dto.response.InstrutorResponse;
import br.com.devquote.minicurso.dto.response.ModuloSimplificadoResponse;
import br.com.devquote.minicurso.entity.InstrutorMinicurso;
import br.com.devquote.minicurso.entity.ModuloEvento;
import br.com.devquote.minicurso.repository.InstrutorMinicursoRepository;
import br.com.devquote.minicurso.repository.ModuloEventoRepository;
import br.com.devquote.service.storage.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InstrutorMinicursoService {

    private final InstrutorMinicursoRepository instrutorRepository;
    private final ModuloEventoRepository moduloRepository;
    private final FileStorageStrategy fileStorageStrategy;

    private static final String FOTO_PATH_PREFIX = "minicurso/instrutores/";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @Transactional(readOnly = true)
    public List<InstrutorResponse> listarTodos() {
        List<InstrutorMinicurso> instrutores = instrutorRepository.findAllByOrderByOrdemAscNomeAsc();
        return instrutores.stream()
                .map(this::toResponseWithFotoUrlSimple)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InstrutorResponse> listarAtivos() {
        return instrutorRepository.findByAtivoTrueOrderByOrdemAscNomeAsc()
                .stream()
                .map(this::toResponseWithFotoUrl)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InstrutorResponse> listarPorModulo(Long moduloId) {
        return instrutorRepository.findByModulosIdAndAtivoTrue(moduloId)
                .stream()
                .map(this::toResponseWithFotoUrl)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InstrutorResponse> listarInstrutoresGerais() {
        return instrutorRepository.findInstrutoresGeraisAtivos()
                .stream()
                .map(this::toResponseWithFotoUrl)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InstrutorResponse buscarPorId(Long id) {
        InstrutorMinicurso instrutor = findById(id);
        return toResponseWithFotoUrl(instrutor);
    }

    public InstrutorResponse criar(InstrutorRequest request) {
        InstrutorMinicurso instrutor = InstrutorMinicursoAdapter.toEntity(request);

        if (request.getModulosIds() != null && !request.getModulosIds().isEmpty()) {
            Set<ModuloEvento> modulos = new HashSet<>(moduloRepository.findAllById(request.getModulosIds()));
            instrutor.setModulos(modulos);
        }

        instrutor = instrutorRepository.save(instrutor);
        log.info("Instrutor criado com sucesso: {}", instrutor.getId());

        return InstrutorMinicursoAdapter.toResponseDTO(instrutor);
    }

    public InstrutorResponse atualizar(Long id, InstrutorRequest request) {
        InstrutorMinicurso instrutor = findById(id);

        InstrutorMinicursoAdapter.updateEntityFromDto(request, instrutor);

        if (request.getModulosIds() != null) {
            Set<ModuloEvento> modulos = new HashSet<>(moduloRepository.findAllById(request.getModulosIds()));
            instrutor.setModulos(modulos);
        }

        instrutor = instrutorRepository.save(instrutor);
        log.info("Instrutor atualizado com sucesso: {}", instrutor.getId());

        return toResponseWithFotoUrl(instrutor);
    }

    public InstrutorResponse atualizarFoto(Long id, MultipartFile foto) {
        InstrutorMinicurso instrutor = findById(id);

        validarArquivoFoto(foto);

        if (instrutor.getFotoUrl() != null && !instrutor.getFotoUrl().isEmpty()) {
            fileStorageStrategy.deleteFile(instrutor.getFotoUrl());
        }

        try {
            String extensao = getExtensao(foto.getOriginalFilename());
            String path = FOTO_PATH_PREFIX + id + "/foto" + extensao;
            String fotoKey = fileStorageStrategy.uploadFile(foto, path);
            instrutor.setFotoUrl(fotoKey);
            instrutor = instrutorRepository.save(instrutor);
            log.info("Foto do instrutor {} atualizada com sucesso", id);
        } catch (IOException e) {
            log.error("Erro ao fazer upload da foto do instrutor {}: {}", id, e.getMessage());
            throw new BusinessException("Erro ao fazer upload da foto");
        }

        return toResponseWithFotoUrl(instrutor);
    }

    public InstrutorResponse removerFoto(Long id) {
        InstrutorMinicurso instrutor = findById(id);

        if (instrutor.getFotoUrl() != null && !instrutor.getFotoUrl().isEmpty()) {
            fileStorageStrategy.deleteFile(instrutor.getFotoUrl());
            instrutor.setFotoUrl(null);
            instrutor = instrutorRepository.save(instrutor);
            log.info("Foto do instrutor {} removida com sucesso", id);
        }

        return InstrutorMinicursoAdapter.toResponseDTO(instrutor);
    }

    public void excluir(Long id) {
        InstrutorMinicurso instrutor = findById(id);

        // Remover foto do storage
        if (instrutor.getFotoUrl() != null && !instrutor.getFotoUrl().isEmpty()) {
            fileStorageStrategy.deleteFile(instrutor.getFotoUrl());
        }

        // Remover vinculos com modulos (query nativa)
        instrutorRepository.deleteVinculosModulos(id);

        instrutorRepository.delete(instrutor);
        log.info("Instrutor {} excluido com sucesso", id);
    }

    public void vincularModulos(Long instrutorId, List<Long> modulosIds) {
        InstrutorMinicurso instrutor = findById(instrutorId);

        Set<ModuloEvento> modulos = new HashSet<>(moduloRepository.findAllById(modulosIds));
        instrutor.setModulos(modulos);
        instrutorRepository.save(instrutor);

        log.info("Modulos vinculados ao instrutor {}: {}", instrutorId, modulosIds);
    }

    private InstrutorMinicurso findById(Long id) {
        return instrutorRepository.findByIdWithModulos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instrutor nao encontrado com id: " + id));
    }

    private InstrutorResponse toResponseWithFotoUrl(InstrutorMinicurso instrutor) {
        InstrutorResponse response = InstrutorMinicursoAdapter.toResponseDTO(instrutor);

        if (instrutor.getFotoUrl() != null && !instrutor.getFotoUrl().isEmpty()) {
            String presignedUrl = fileStorageStrategy.getFileUrl(instrutor.getFotoUrl());
            response.setFotoUrl(presignedUrl);
        }

        return response;
    }

    private InstrutorResponse toResponseWithFotoUrlSimple(InstrutorMinicurso instrutor) {
        List<ModuloSimplificadoResponse> modulos = moduloRepository.findModulosByInstrutorId(instrutor.getId());

        InstrutorResponse response = InstrutorResponse.builder()
                .id(instrutor.getId())
                .nome(instrutor.getNome())
                .localTrabalho(instrutor.getLocalTrabalho())
                .tempoCarreira(instrutor.getTempoCarreira())
                .miniBio(instrutor.getMiniBio())
                .fotoUrl(instrutor.getFotoUrl())
                .email(instrutor.getEmail())
                .linkedin(instrutor.getLinkedin())
                .ativo(instrutor.getAtivo())
                .ordem(instrutor.getOrdem())
                .createdAt(instrutor.getCreatedAt())
                .updatedAt(instrutor.getUpdatedAt())
                .modulos(modulos)
                .build();

        if (instrutor.getFotoUrl() != null && !instrutor.getFotoUrl().isEmpty()) {
            String presignedUrl = fileStorageStrategy.getFileUrl(instrutor.getFotoUrl());
            response.setFotoUrl(presignedUrl);
        }

        return response;
    }

    private void validarArquivoFoto(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new BusinessException("Arquivo de foto e obrigatorio");
        }

        if (foto.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("Arquivo de foto deve ter no maximo 2MB");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(foto.getContentType())) {
            throw new BusinessException("Formato de foto invalido. Formatos permitidos: JPEG, PNG, WebP");
        }
    }

    private String getExtensao(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
