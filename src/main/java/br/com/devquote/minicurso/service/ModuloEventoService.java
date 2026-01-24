package br.com.devquote.minicurso.service;

import br.com.devquote.minicurso.adapter.ModuloEventoAdapter;
import br.com.devquote.minicurso.dto.request.ModuloEventoRequest;
import br.com.devquote.minicurso.dto.response.ItemModuloResponse;
import br.com.devquote.minicurso.dto.response.ModuloEventoResponse;
import br.com.devquote.minicurso.entity.ConfiguracaoEvento;
import br.com.devquote.minicurso.entity.ModuloEvento;
import br.com.devquote.minicurso.repository.ConfiguracaoEventoRepository;
import br.com.devquote.minicurso.repository.ModuloEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ModuloEventoService {

    private final ModuloEventoRepository moduloEventoRepository;
    private final ConfiguracaoEventoRepository configuracaoEventoRepository;
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

        return ModuloEventoAdapter.toResponseDTO(entity, List.of());
    }

    public ModuloEventoResponse atualizar(Long id, ModuloEventoRequest request) {
        ModuloEvento entity = moduloEventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modulo nao encontrado"));

        ModuloEventoAdapter.updateEntityFromDto(request, entity);
        entity = moduloEventoRepository.save(entity);

        return toResponseWithItens(entity);
    }

    public void excluir(Long id) {
        if (!moduloEventoRepository.existsById(id)) {
            throw new RuntimeException("Modulo nao encontrado");
        }
        moduloEventoRepository.deleteById(id);
    }

    private ModuloEventoResponse toResponseWithItens(ModuloEvento entity) {
        List<ItemModuloResponse> itens = itemModuloService.listarPorModulo(entity.getId());
        return ModuloEventoAdapter.toResponseDTO(entity, itens);
    }

    private ModuloEventoResponse toResponseWithItensAtivos(ModuloEvento entity) {
        List<ItemModuloResponse> itens = itemModuloService.listarAtivosPorModulo(entity.getId());
        return ModuloEventoAdapter.toResponseDTO(entity, itens);
    }
}
