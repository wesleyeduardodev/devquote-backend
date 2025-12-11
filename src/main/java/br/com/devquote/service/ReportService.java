package br.com.devquote.service;

import br.com.devquote.dto.request.OperationalReportRequest;

public interface ReportService {

    byte[] generateOperationalReportPdf(OperationalReportRequest request);

    byte[] generateTaskReportPdf(Long taskId, boolean showValues);
}
