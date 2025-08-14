package br.com.devquote.error;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

final class ProblemDetailsUtils {

    static final String TRACE_ID_KEY = "traceId";

    private ProblemDetailsUtils() {}

    static ProblemDetail baseProblem(HttpStatus status, String title, String detail, String type, String instancePath) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        if (title != null && !title.isBlank()) {
            pd.setTitle(title);
        }
        if (type != null && !type.isBlank()) {
            pd.setType(URI.create(type));
        } else {
            pd.setType(URI.create("about:blank"));
        }
        if (instancePath != null && !instancePath.isBlank()) {
            pd.setProperty("instance", instancePath);
        }

        pd.setProperty("timestamp", OffsetDateTime.now().toString());

        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        pd.setProperty(TRACE_ID_KEY, traceId);
        return pd;
    }

    static void addProperties(ProblemDetail pd, Map<String, Object> extra) {
        if (extra == null) return;
        extra.forEach(pd::setProperty);
    }
}
