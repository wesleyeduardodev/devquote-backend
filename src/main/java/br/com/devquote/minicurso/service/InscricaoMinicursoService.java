package br.com.devquote.minicurso.service;

import br.com.devquote.minicurso.adapter.InscricaoMinicursoAdapter;
import br.com.devquote.minicurso.dto.request.InscricaoRequest;
import br.com.devquote.minicurso.dto.response.InscricaoResponse;
import br.com.devquote.minicurso.entity.ConfiguracaoEvento;
import br.com.devquote.minicurso.entity.InscricaoMinicurso;
import br.com.devquote.minicurso.repository.ConfiguracaoEventoRepository;
import br.com.devquote.minicurso.repository.InscricaoMinicursoRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class InscricaoMinicursoService {

    private final InscricaoMinicursoRepository inscricaoRepository;
    private final ConfiguracaoEventoRepository configuracaoEventoRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public InscricaoResponse criar(InscricaoRequest request) {
        String emailNormalizado = request.getEmail().toLowerCase().trim();

        if (inscricaoRepository.existsByEmail(emailNormalizado)) {
            throw new RuntimeException("Email ja cadastrado");
        }

        ConfiguracaoEvento config = configuracaoEventoRepository.findFirstByOrderByIdDesc()
                .orElse(null);

        if (config != null) {
            if (!Boolean.TRUE.equals(config.getInscricoesAbertas())) {
                throw new RuntimeException("Inscricoes encerradas");
            }

            if (config.getQuantidadeVagas() != null) {
                long totalInscritos = inscricaoRepository.count();
                if (totalInscritos >= config.getQuantidadeVagas()) {
                    throw new RuntimeException("Vagas esgotadas");
                }
            }
        }

        InscricaoMinicurso entity = InscricaoMinicursoAdapter.toEntity(request);
        entity = inscricaoRepository.save(entity);

        return InscricaoMinicursoAdapter.toResponseDTO(entity);
    }

    public boolean verificarEmailExiste(String email) {
        return inscricaoRepository.existsByEmail(email.toLowerCase().trim());
    }

    public List<InscricaoResponse> listarTodas() {
        return inscricaoRepository.findAll().stream()
                .map(InscricaoMinicursoAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    public InscricaoResponse buscarPorId(Long id) {
        InscricaoMinicurso entity = inscricaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscricao nao encontrada"));

        return InscricaoMinicursoAdapter.toResponseDTO(entity);
    }

    public void excluir(Long id) {
        if (!inscricaoRepository.existsById(id)) {
            throw new RuntimeException("Inscricao nao encontrada");
        }
        inscricaoRepository.deleteById(id);
    }

    public long contarInscritos() {
        return inscricaoRepository.count();
    }

    public byte[] exportarExcel() throws IOException {
        List<InscricaoMinicurso> inscricoes = inscricaoRepository.findAll();
        inscricoes.sort(Comparator.comparing(InscricaoMinicurso::getNome, String.CASE_INSENSITIVE_ORDER));

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Inscricoes");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Nome", "Email", "Telefone", "Curso", "Periodo", "Nivel", "Expectativa", "Data Inscricao", "Confirmado"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (InscricaoMinicurso inscricao : inscricoes) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(inscricao.getId());
                row.createCell(1).setCellValue(inscricao.getNome());
                row.createCell(2).setCellValue(inscricao.getEmail());
                row.createCell(3).setCellValue(inscricao.getTelefone() != null ? inscricao.getTelefone() : "");
                row.createCell(4).setCellValue(inscricao.getCurso());
                row.createCell(5).setCellValue(inscricao.getPeriodo() != null ? inscricao.getPeriodo() : "");
                row.createCell(6).setCellValue(inscricao.getNivelProgramacao());
                row.createCell(7).setCellValue(inscricao.getExpectativa() != null ? inscricao.getExpectativa() : "");
                row.createCell(8).setCellValue(inscricao.getCreatedAt() != null ? inscricao.getCreatedAt().format(DATE_FORMATTER) : "");
                row.createCell(9).setCellValue(Boolean.TRUE.equals(inscricao.getConfirmado()) ? "Sim" : "Nao");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportarPdf() throws JRException {
        List<InscricaoMinicurso> inscricoes = inscricaoRepository.findAll();
        inscricoes.sort(Comparator.comparing(InscricaoMinicurso::getNome, String.CASE_INSENSITIVE_ORDER));

        InputStream reportStream = getClass().getResourceAsStream("/reports/inscricoes_minicurso_report.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("titulo", "Lista de Inscricoes");
        parameters.put("dataGeracao", LocalDateTime.now());
        parameters.put("totalRegistros", inscricoes.size());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(inscricoes);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
