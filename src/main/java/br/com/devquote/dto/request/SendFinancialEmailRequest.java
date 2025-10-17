package br.com.devquote.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendFinancialEmailRequest {
    private List<String> additionalEmails;
}
