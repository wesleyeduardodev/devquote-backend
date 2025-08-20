package br.com.devquote.configuration;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class BillingProperties {

    @Value("${devquote.billing.payment-day:20}")
    private int paymentDay;
}
