package br.com.devquote.dto.request;

import br.com.devquote.enums.Environment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEnvironmentRequest {

    private Environment environment;
}
