package br.com.devquote.minicurso.service;

import br.com.devquote.minicurso.adapter.ModuloEventoAdapter;
import br.com.devquote.minicurso.dto.request.ModuloEventoRequest;
import br.com.devquote.minicurso.dto.response.InstrutorSimplificadoResponse;
import br.com.devquote.minicurso.dto.response.ItemModuloResponse;
import br.com.devquote.minicurso.dto.response.ModuloEventoResponse;
import br.com.devquote.minicurso.entity.ConfiguracaoEvento;
import br.com.devquote.minicurso.entity.InstrutorMinicurso;
import br.com.devquote.minicurso.entity.ModuloEvento;
import br.com.devquote.minicurso.repository.ConfiguracaoEventoRepository;
import br.com.devquote.minicurso.repository.InstrutorMinicursoRepository;
import br.com.devquote.minicurso.repository.ModuloEventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ModuloEventoService {

    private final ModuloEventoRepository moduloEventoRepository;
    private final ConfiguracaoEventoRepository configuracaoEventoRepository;
    private final InstrutorMinicursoRepository instrutorRepository;
    private final ItemModuloService itemModuloService;

    public List<ModuloEventoResponse> listarPorEvento(Long eventoId) {
        return moduloEventoRepository.findByConfiguracaoEventoIdOrderByOrdemAsc(eventoId).stream()
                .map(this::toResponseWithItens)
                .collect(Collectors.toList());
    }

    public List<ModuloEventoResponse> listarAtivosPorEvento(Long eventoId) {
        return moduloEventoRepository.findByConfiguracaoEventoIdAndAtivoTrueOrderByOrdemAsc(eventoId).stream()
                .map(this::toResponseWithItensAtivos)
                .collect(Collectors.toList());
    }

    public ModuloEventoResponse buscarPorId(Long id) {
        ModuloEvento entity = moduloEventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modulo nao encontrado"));

        return toResponseWithItens(entity);
    }

    public ModuloEventoResponse criar(Long eventoId, ModuloEventoRequest request) {
        ConfiguracaoEvento configuracao = configuracaoEventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Configuracao do evento nao encontrada"));

        ModuloEvento entity = ModuloEventoAdapter.toEntity(request, configuracao);
        entity = moduloEventoRepository.save(entity);

        vincularInstrutores(entity, request.getInstrutoresIds());

        log.info("Modulo criado: {}", entity.getId());
        return ModuloEventoAdapter.toResponseDTO(entity, List.of());
    }

    public ModuloEventoResponse atualizar(Long id, ModuloEventoRequest request) {
        ModuloEvento entity = moduloEventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modulo nao encontrado"));

        ModuloEventoAdapter.updateEntityFromDto(request, entity);
        entity = moduloEventoRepository.save(entity);

        vincularInstrutores(entity, request.getInstrutoresIds());

        log.info("Modulo atualizado: {}", entity.getId());
        return toResponseWithItens(entity);
    }

    private void vincularInstrutores(ModuloEvento modulo, List<Long> instrutoresIds) {
        // Remover modulo de todos os instrutores atuais
        List<InstrutorMinicurso> instrutoresAtuais = instrutorRepository.findByModulosIdAndAtivoTrue(modulo.getId());
        for (InstrutorMinicurso instrutor : instrutoresAtuais) {
            instrutor.getModulos().remove(modulo);
            instrutorRepository.save(instrutor);
        }

        // Adicionar modulo aos novos instrutores
        if (instrutoresIds != null && !instrutoresIds.isEmpty()) {
            List<InstrutorMinicurso> novosInstrutores = instrutorRepository.findAllById(instrutoresIds);
            for (InstrutorMinicurso instrutor : novosInstrutores) {
                if (instrutor.getModulos() == null) {
                    instrutor.setModulos(new HashSet<>());
                }
                instrutor.getModulos().add(modulo);
                instrutorRepository.save(instrutor);
            }
            log.info("Instrutores vinculados ao modulo {}: {}", modulo.getId(), instrutoresIds);
        }
    }

    public void excluir(Long id) {
        if (!moduloEventoRepository.existsById(id)) {
            throw new RuntimeException("Modulo nao encontrado");
        }
        moduloEventoRepository.deleteById(id);
    }

    private ModuloEventoResponse toResponseWithItens(ModuloEvento entity) {
        List<ItemModuloResponse> itens = itemModuloService.listarPorModulo(entity.getId());
        List<InstrutorSimplificadoResponse> instrutores = buscarInstrutoresDoModulo(entity.getId());
        return ModuloEventoAdapter.toResponseDTO(entity, itens, instrutores);
    }

    private ModuloEventoResponse toResponseWithItensAtivos(ModuloEvento entity) {
        List<ItemModuloResponse> itens = itemModuloService.listarAtivosPorModulo(entity.getId());
        List<InstrutorSimplificadoResponse> instrutores = buscarInstrutoresDoModulo(entity.getId());
        return ModuloEventoAdapter.toResponseDTO(entity, itens, instrutores);
    }

    private List<InstrutorSimplificadoResponse> buscarInstrutoresDoModulo(Long moduloId) {
        return instrutorRepository.findByModulosIdAndAtivoTrue(moduloId).stream()
                .map(i -> InstrutorSimplificadoResponse.builder()
                        .id(i.getId())
                        .nome(i.getNome())
                        .build())
                .collect(Collectors.toList());
    }
}
