package br.com.devquote.minicurso.service;

import br.com.devquote.minicurso.adapter.ItemModuloAdapter;
import br.com.devquote.minicurso.dto.request.ItemModuloRequest;
import br.com.devquote.minicurso.dto.response.ItemModuloResponse;
import br.com.devquote.minicurso.entity.ItemModulo;
import br.com.devquote.minicurso.entity.ModuloEvento;
import br.com.devquote.minicurso.repository.ItemModuloRepository;
import br.com.devquote.minicurso.repository.ModuloEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemModuloService {

    private final ItemModuloRepository itemModuloRepository;
    private final ModuloEventoRepository moduloEventoRepository;

    public List<ItemModuloResponse> listarPorModulo(Long moduloId) {
        return itemModuloRepository.findByModuloIdOrderByOrdemAsc(moduloId).stream()
                .map(ItemModuloAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ItemModuloResponse> listarAtivosPorModulo(Long moduloId) {
        return itemModuloRepository.findByModuloIdAndAtivoTrueOrderByOrdemAsc(moduloId).stream()
                .map(ItemModuloAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ItemModuloResponse criar(Long moduloId, ItemModuloRequest request) {
        ModuloEvento modulo = moduloEventoRepository.findById(moduloId)
                .orElseThrow(() -> new RuntimeException("Modulo nao encontrado"));

        ItemModulo entity = ItemModuloAdapter.toEntity(request, modulo);
        entity = itemModuloRepository.save(entity);

        return ItemModuloAdapter.toResponseDTO(entity);
    }

    public ItemModuloResponse atualizar(Long id, ItemModuloRequest request) {
        ItemModulo entity = itemModuloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item nao encontrado"));

        ItemModuloAdapter.updateEntityFromDto(request, entity);
        entity = itemModuloRepository.save(entity);

        return ItemModuloAdapter.toResponseDTO(entity);
    }

    public void excluir(Long id) {
        if (!itemModuloRepository.existsById(id)) {
            throw new RuntimeException("Item nao encontrado");
        }
        itemModuloRepository.deleteById(id);
    }
}
