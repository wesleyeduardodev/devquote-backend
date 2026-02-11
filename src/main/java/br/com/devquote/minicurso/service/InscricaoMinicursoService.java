package br.com.devquote.minicurso.service;

import br.com.devquote.error.BusinessException;
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
import org.apache.poi.ss.usermodel.CreationHelper;
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
            throw new BusinessException("Este e-mail já está inscrito.", "EMAIL_DUPLICADO");
        }

        ConfiguracaoEvento config = configuracaoEventoRepository.findFirstByOrderByIdDesc()
                .orElse(null);

        boolean confirmado = true;

        if (config != null) {
            if (!Boolean.TRUE.equals(config.getInscricoesAbertas())) {
                throw new BusinessException("As inscrições para este evento foram encerradas.", "INSCRICOES_ENCERRADAS");
            }

            if (config.getQuantidadeVagas() != null) {
                long totalInscritos = inscricaoRepository.count();
                if (totalInscritos >= config.getQuantidadeVagas()) {
                    confirmado = false;
                }
            }
        }

        InscricaoMinicurso entity = InscricaoMinicursoAdapter.toEntity(request);
        entity.setConfirmado(confirmado);
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

    public void promoverListaEspera(int quantidadeVagas) {
        long confirmados = inscricaoRepository.countByConfirmado(true);
        if (confirmados >= quantidadeVagas) {
            return;
        }

        long vagasLivres = quantidadeVagas - confirmados;
        List<InscricaoMinicurso> listaEspera = inscricaoRepository.findByConfirmadoOrderByCreatedAtAsc(false);

        listaEspera.stream()
                .limit(vagasLivres)
                .forEach(inscricao -> inscricao.setConfirmado(true));

        inscricaoRepository.saveAll(
                listaEspera.stream()
                        .limit(vagasLivres)
                        .collect(Collectors.toList())
        );
    }

    public byte[] exportarExcel() throws IOException {
        List<InscricaoMinicurso> inscricoes = inscricaoRepository.findAll();
        inscricoes.sort(Comparator.comparing(InscricaoMinicurso::getCreatedAt));

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Inscrições");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CreationHelper createHelper = workbook.getCreationHelper();
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Nome", "Email", "Telefone", "Curso", "Período", "Nível", "Expectativa", "Data Inscrição", "Status"};

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
                Cell dateCell = row.createCell(8);
                if (inscricao.getCreatedAt() != null) {
                    dateCell.setCellValue(java.sql.Timestamp.valueOf(inscricao.getCreatedAt()));
                    dateCell.setCellStyle(dateStyle);
                }
                row.createCell(9).setCellValue(Boolean.TRUE.equals(inscricao.getConfirmado()) ? "Confirmado" : "Lista de espera");
            }

            int[] columnWidths = {8, 32, 38, 16, 30, 14, 18, 40, 22, 16};
            for (int i = 0; i < columnWidths.length; i++) {
                sheet.setColumnWidth(i, columnWidths[i] * 256);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportarPdf() throws JRException {
        List<InscricaoMinicurso> inscricoes = inscricaoRepository.findAll();
        inscricoes.sort(Comparator.comparing(InscricaoMinicurso::getCreatedAt));

        InputStream reportStream = getClass().getResourceAsStream("/reports/inscricoes_minicurso_report.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("titulo", "Lista de Inscrições");
        parameters.put("dataGeracao", LocalDateTime.now());
        parameters.put("totalRegistros", inscricoes.size());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(inscricoes);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
