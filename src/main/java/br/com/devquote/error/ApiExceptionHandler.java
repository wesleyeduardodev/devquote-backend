package br.com.devquote.error;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> extra = new HashMap<>();
        List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.<String, Object>of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage(),
                        "rejectedValue", fe.getRejectedValue()
                ))
                .collect(Collectors.toList());
        extra.put("errors", errors);

        ProblemDetail pd = ProblemDetailsUtils.baseProblem(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "Um ou mais campos são inválidos.",
                "https://api.devquote.com/errors/validation",
                req.getRequestURI()
        );
        ProblemDetailsUtils.addProperties(pd, extra);
        return pd;
    }

    @ExceptionHandler(BindException.class)
    ProblemDetail handleBindException(BindException ex, HttpServletRequest req) {
        Map<String, Object> extra = new HashMap<>();
        List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.<String, Object>of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage(),
                        "rejectedValue", fe.getRejectedValue()
                ))
                .collect(Collectors.toList());
        extra.put("errors", errors);

        ProblemDetail pd = ProblemDetailsUtils.baseProblem(
                HttpStatus.BAD_REQUEST,
                "Binding failed",
                "Não foi possível vincular os parâmetros da requisição.",
                "https://api.devquote.com/errors/binding",
                req.getRequestURI()
        );
        ProblemDetailsUtils.addProperties(pd, extra);
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        List<Map<String, Object>> errors = ex.getConstraintViolations().stream()
                .map(this::toViolationMap)
                .collect(Collectors.toList());

        ProblemDetail pd = ProblemDetailsUtils.baseProblem(
                HttpStatus.BAD_REQUEST,
                "Constraint violation",
                "Parâmetros inválidos na requisição.",
                "https://api.devquote.com/errors/constraint-violation",
                req.getRequestURI()
        );
        pd.setProperty("errors", errors);
        return pd;
    }

    private Map<String, Object> toViolationMap(ConstraintViolation<?> cv) {
        String path = cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : null;
        return Map.of(
                "field", path,
                "message", cv.getMessage(),
                "rejectedValue", cv.getInvalidValue()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ProblemDetail handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailsUtils.baseProblem(
                HttpStatus.BAD_REQUEST,
                "Missing parameter",
                "Parâmetro obrigatório ausente: " + ex.getParameterName(),
                "https://api.devquote.com/errors/missing-parameter",
                req.getRequestURI()
        );
        return pd;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailsUtils.baseProblem(
                HttpStatus.BAD_REQUEST,
                "Type mismatch",
                "Tipo inválido para o parâmetro '" + ex.getName() + "'.",
                "https://api.devquote.com/errors/type-mismatch",
                req.getRequestURI()
        );
        pd.setProperty("expectedType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : null);
        pd.setProperty("rejectedValue", ex.getValue());
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailsUtils.baseProblem(
                HttpStatus.BAD_REQUEST,
                "Malformed request",
                "Corpo da requisição inválido ou malformado.",
                "https://api.devquote.com/errors/malformed-request",
                req.getRequestURI()
        );
        return pd;
    }

    @ExceptionHandler({NoSuchElementException.class, EntityNotFoundException.class})
    ProblemDetail handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return ProblemDetailsUtils.baseProblem(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                ex.getMessage() != null ? ex.getMessage() : "Recurso não encontrado.",
                "https://api.devquote.com/errors/not-found",
                req.getRequestURI()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailsUtils.baseProblem(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                ex.getMessage(),
                "https://api.devquote.com/errors/resource-not-found",
                req.getRequestURI()
        );
        pd.setProperty("resourceType", ex.getResourceType());
        pd.setProperty("resourceId", ex.getResourceId());
        return pd;
    }

    @ExceptionHandler(BusinessException.class)
    ProblemDetail handleBusinessException(BusinessException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailsUtils.baseProblem(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Business rule violation",
                ex.getMessage(),
                "https://api.devquote.com/errors/business-error",
                req.getRequestURI()
        );
        pd.setProperty("errorCode", ex.getErrorCode());
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return ProblemDetailsUtils.baseProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid argument",
                ex.getMessage() != null ? ex.getMessage() : "Argumento inválido fornecido.",
                "https://api.devquote.com/errors/invalid-argument",
                req.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        return ProblemDetailsUtils.baseProblem(
                HttpStatus.CONFLICT,
                "Invalid state",
                ex.getMessage() != null ? ex.getMessage() : "Operação não permitida no estado atual.",
                "https://api.devquote.com/errors/invalid-state",
                req.getRequestURI()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        String message = "Operação violou restrições de integridade do banco.";
        String rootCauseMessage = ex.getMostSpecificCause().getMessage();
        
        // Interpreta mensagens de erro comuns do PostgreSQL
        if (rootCauseMessage != null) {
            if (rootCauseMessage.contains("duplicate key")) {
                message = "Já existe um registro com essas informações.";
            } else if (rootCauseMessage.contains("foreign key constraint")) {
                message = "Não é possível realizar essa operação pois o registro está sendo referenciado por outros dados.";
            } else if (rootCauseMessage.contains("not null constraint")) {
                message = "Campo obrigatório não foi informado.";
            }
        }
        
        ProblemDetail pd = ProblemDetailsUtils.baseProblem(
                HttpStatus.CONFLICT,
                "Data integrity violation",
                message,
                "https://api.devquote.com/errors/conflict",
                req.getRequestURI()
        );
        pd.setProperty("rootCause", rootCauseMessage);
        return pd;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailsUtils.baseProblem(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method not allowed",
                "Método HTTP não suportado para este endpoint.",
                "https://api.devquote.com/errors/method-not-allowed",
                req.getRequestURI()
        );
        pd.setProperty("supportedMethods", ex.getSupportedHttpMethods());
        return pd;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ProblemDetail handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailsUtils.baseProblem(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported media type",
                "Tipo de mídia não suportado.",
                "https://api.devquote.com/errors/unsupported-media-type",
                req.getRequestURI()
        );
        pd.setProperty("supportedMediaTypes", ex.getSupportedMediaTypes());
        return pd;
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        return ProblemDetailsUtils.baseProblem(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Autenticação necessária ou inválida.",
                "https://api.devquote.com/errors/unauthorized",
                req.getRequestURI()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailsUtils.baseProblem(
                HttpStatus.FORBIDDEN,
                "Access denied",
                "Você não tem permissão para acessar esse recurso.",
                "https://api.devquote.com/errors/forbidden",
                req.getRequestURI()
        );
        return pd;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
        return ProblemDetailsUtils.baseProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal error",
                "Erro inesperado. Já estamos trabalhando para corrigir.",
                "https://api.devquote.com/errors/internal",
                req.getRequestURI()
        );
    }
}
