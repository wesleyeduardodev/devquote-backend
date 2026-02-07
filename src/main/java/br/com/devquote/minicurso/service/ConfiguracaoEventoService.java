package br.com.devquote.minicurso.service;

import br.com.devquote.minicurso.adapter.ConfiguracaoEventoAdapter;
import br.com.devquote.minicurso.adapter.DataEventoAdapter;
import br.com.devquote.minicurso.dto.request.ConfiguracaoEventoRequest;
import br.com.devquote.minicurso.dto.request.DataEventoRequest;
import br.com.devquote.minicurso.dto.response.ConfiguracaoEventoResponse;
import br.com.devquote.minicurso.dto.response.DataEventoResponse;
import br.com.devquote.minicurso.dto.response.ModuloEventoResponse;
import br.com.devquote.minicurso.entity.ConfiguracaoEvento;
import br.com.devquote.minicurso.entity.DataEvento;
import br.com.devquote.minicurso.repository.ConfiguracaoEventoRepository;
import br.com.devquote.minicurso.repository.DataEventoRepository;
import br.com.devquote.minicurso.repository.InscricaoMinicursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ConfiguracaoEventoService {

    private final ConfiguracaoEventoRepository configuracaoEventoRepository;
    private final InscricaoMinicursoRepository inscricaoMinicursoRepository;
    private final ModuloEventoService moduloEventoService;
    private final DataEventoRepository dataEventoRepository;

    public ConfiguracaoEventoResponse obterConfiguracao() {
        ConfiguracaoEvento config = configuracaoEventoRepository.findFirstByOrderByIdDesc()
                .orElse(null);

        if (config == null) {
            return null;
        }

        long totalInscritos = inscricaoMinicursoRepository.count();
        List<ModuloEventoResponse> modulos = moduloEventoService.listarAtivosPorEvento(config.getId());
        List<DataEventoResponse> datasEvento = DataEventoAdapter.toResponseDTOList(
                dataEventoRepository.findByConfiguracaoEventoIdOrderByOrdemAsc(config.getId()));

        return ConfiguracaoEventoAdapter.toResponseDTO(config, totalInscritos, modulos, datasEvento);
    }

    public ConfiguracaoEventoResponse obterConfiguracaoCompleta() {
        ConfiguracaoEvento config = configuracaoEventoRepository.findFirstByOrderByIdDesc()
                .orElse(null);

        if (config == null) {
            return null;
        }

        long totalInscritos = inscricaoMinicursoRepository.count();
        List<ModuloEventoResponse> modulos = moduloEventoService.listarPorEvento(config.getId());
        List<DataEventoResponse> datasEvento = DataEventoAdapter.toResponseDTOList(
                dataEventoRepository.findByConfiguracaoEventoIdOrderByOrdemAsc(config.getId()));

        return ConfiguracaoEventoAdapter.toResponseDTO(config, totalInscritos, modulos, datasEvento);
    }

    public ConfiguracaoEventoResponse atualizar(ConfiguracaoEventoRequest request) {
        ConfiguracaoEvento config = configuracaoEventoRepository.findFirstByOrderByIdDesc()
                .orElseGet(() -> ConfiguracaoEvento.builder()
                        .titulo(request.getTitulo())
                        .inscricoesAbertas(true)
                        .build());

        ConfiguracaoEventoAdapter.updateEntityFromDto(request, config);
        config = configuracaoEventoRepository.save(config);

        sincronizarDatasEvento(config, request.getDatasEvento());

        long totalInscritos = inscricaoMinicursoRepository.count();
        List<ModuloEventoResponse> modulos = moduloEventoService.listarPorEvento(config.getId());
        List<DataEventoResponse> datasEvento = DataEventoAdapter.toResponseDTOList(
                dataEventoRepository.findByConfiguracaoEventoIdOrderByOrdemAsc(config.getId()));

        return ConfiguracaoEventoAdapter.toResponseDTO(config, totalInscritos, modulos, datasEvento);
    }

    public Long obterEventoIdAtual() {
        return configuracaoEventoRepository.findFirstByOrderByIdDesc()
                .map(ConfiguracaoEvento::getId)
                .orElse(null);
    }

    private void sincronizarDatasEvento(ConfiguracaoEvento config, List<DataEventoRequest> datasRequest) {
        if (datasRequest == null) {
            return;
        }

        List<DataEvento> datasExistentes = dataEventoRepository
                .findByConfiguracaoEventoIdOrderByOrdemAsc(config.getId());

        Set<Long> idsRecebidos = datasRequest.stream()
                .map(DataEventoRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        datasExistentes.stream()
                .filter(d -> !idsRecebidos.contains(d.getId()))
                .forEach(dataEventoRepository::delete);

        for (DataEventoRequest dataRequest : datasRequest) {
            if (dataRequest.getId() != null) {
                dataEventoRepository.findById(dataRequest.getId())
                        .ifPresent(existing -> {
                            DataEventoAdapter.updateEntityFromDto(dataRequest, existing);
                            dataEventoRepository.save(existing);
                        });
            } else {
                DataEvento novaData = DataEventoAdapter.toEntity(dataRequest, config);
                dataEventoRepository.save(novaData);
            }
        }
    }
}
