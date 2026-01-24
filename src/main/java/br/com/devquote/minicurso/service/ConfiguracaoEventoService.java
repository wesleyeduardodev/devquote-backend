package br.com.devquote.minicurso.service;

import br.com.devquote.minicurso.adapter.ConfiguracaoEventoAdapter;
import br.com.devquote.minicurso.dto.request.ConfiguracaoEventoRequest;
import br.com.devquote.minicurso.dto.response.ConfiguracaoEventoResponse;
import br.com.devquote.minicurso.dto.response.ModuloEventoResponse;
import br.com.devquote.minicurso.entity.ConfiguracaoEvento;
import br.com.devquote.minicurso.repository.ConfiguracaoEventoRepository;
import br.com.devquote.minicurso.repository.InscricaoMinicursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ConfiguracaoEventoService {

    private final ConfiguracaoEventoRepository configuracaoEventoRepository;
    private final InscricaoMinicursoRepository inscricaoMinicursoRepository;
    private final ModuloEventoService moduloEventoService;

    public ConfiguracaoEventoResponse obterConfiguracao() {
        ConfiguracaoEvento config = configuracaoEventoRepository.findFirstByOrderByIdDesc()
                .orElse(null);

        if (config == null) {
            return null;
        }

        long totalInscritos = inscricaoMinicursoRepository.count();
        List<ModuloEventoResponse> modulos = moduloEventoService.listarAtivosPorEvento(config.getId());

        return ConfiguracaoEventoAdapter.toResponseDTO(config, totalInscritos, modulos);
    }

    public ConfiguracaoEventoResponse obterConfiguracaoCompleta() {
        ConfiguracaoEvento config = configuracaoEventoRepository.findFirstByOrderByIdDesc()
                .orElse(null);

        if (config == null) {
            return null;
        }

        long totalInscritos = inscricaoMinicursoRepository.count();
        List<ModuloEventoResponse> modulos = moduloEventoService.listarPorEvento(config.getId());

        return ConfiguracaoEventoAdapter.toResponseDTO(config, totalInscritos, modulos);
    }

    public ConfiguracaoEventoResponse atualizar(ConfiguracaoEventoRequest request) {
        ConfiguracaoEvento config = configuracaoEventoRepository.findFirstByOrderByIdDesc()
                .orElseGet(() -> ConfiguracaoEvento.builder()
                        .titulo(request.getTitulo())
                        .inscricoesAbertas(true)
                        .build());

        ConfiguracaoEventoAdapter.updateEntityFromDto(request, config);
        config = configuracaoEventoRepository.save(config);

        long totalInscritos = inscricaoMinicursoRepository.count();
        List<ModuloEventoResponse> modulos = moduloEventoService.listarPorEvento(config.getId());

        return ConfiguracaoEventoAdapter.toResponseDTO(config, totalInscritos, modulos);
    }

    public Long obterEventoIdAtual() {
        return configuracaoEventoRepository.findFirstByOrderByIdDesc()
                .map(ConfiguracaoEvento::getId)
                .orElse(null);
    }
}
