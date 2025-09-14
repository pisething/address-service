package com.piseth.java.school.addressservice.exception;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.DecodingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException; // if you use on MVC; harmless in WebFlux
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    // Problem type URIs (can be real URLs to your error docs)
    private static final URI TYPE_VALIDATION   = URI.create("urn:problem-type:validation-error");
    private static final URI TYPE_BAD_REQUEST  = URI.create("urn:problem-type:bad-request");
    private static final URI TYPE_NOT_FOUND    = URI.create("urn:problem-type:not-found");
    private static final URI TYPE_CONFLICT     = URI.create("urn:problem-type:conflict");
    private static final URI TYPE_UNSUPPORTED  = URI.create("urn:problem-type:unsupported-media-type");
    private static final URI TYPE_NOT_ACCEPTED = URI.create("urn:problem-type:not-acceptable");
    private static final URI TYPE_INTERNAL     = URI.create("urn:problem-type:internal-error");

    /* -------------------- 400s: validation / request issues -------------------- */

    // Bean Validation on @RequestBody (WebFlux)
    @ExceptionHandler(org.springframework.web.bind.support.WebExchangeBindException.class)
    public ProblemDetail handleBind(final org.springframework.web.bind.support.WebExchangeBindException ex,
                                    final ServerWebExchange exchange) {
        logWarn(ex, exchange, "Body validation failed");
        final List<Map<String, Object>> errors = ex.getFieldErrors().stream()
            .map(fe -> {
                final Map<String, Object> m = new LinkedHashMap<>();
                m.put("field", fe.getField());
                m.put("message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value");
                if (fe.getRejectedValue() != null) {
                    m.put("rejectedValue", fe.getRejectedValue());
                }
                return m;
            }).collect(Collectors.toList());

        final ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Validation Failed",
            "Request validation failed", TYPE_VALIDATION, exchange);
        pd.setProperty("errors", errors);
        return pd;
    }

    // Bean Validation on @RequestBody (MVC) – safe to keep for portability
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleBindMvc(final MethodArgumentNotValidException ex, final ServerWebExchange exchange) {
        logWarn(ex, exchange, "Body validation failed (MVC)");
        final List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> {
                final Map<String, Object> m = new LinkedHashMap<>();
                m.put("field", fe.getField());
                m.put("message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value");
                if (fe.getRejectedValue() != null) {
                    m.put("rejectedValue", fe.getRejectedValue());
                }
                return m;
            }).collect(Collectors.toList());

        final ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Validation Failed",
            "Request validation failed", TYPE_VALIDATION, exchange);
        pd.setProperty("errors", errors);
        return pd;
    }

    // Constraint violations on method parameters (@Validated on controllers/services)
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(final ConstraintViolationException ex, final ServerWebExchange exchange) {
        logWarn(ex, exchange, "Parameter validation failed");
        final List<Map<String, Object>> violations = ex.getConstraintViolations().stream()
            .map(v -> {
                final Map<String, Object> m = new LinkedHashMap<>();
                m.put("param", v.getPropertyPath().toString());
                m.put("message", v.getMessage());
                if (v.getInvalidValue() != null) {
                    m.put("rejectedValue", v.getInvalidValue());
                }
                return m;
            }).collect(Collectors.toList());

        final ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Validation Failed",
            "Parameter validation failed", TYPE_VALIDATION, exchange);
        pd.setProperty("errors", violations);
        return pd;
    }

    // Malformed JSON / decoding issues
    @ExceptionHandler({ ServerWebInputException.class, DecodingException.class, CodecException.class })
    public ProblemDetail handleBadJson(final Exception ex, final ServerWebExchange exchange) {
        logWarn(ex, exchange, "Bad request payload");
        final String msg = safeMessage(ex);
        return base(HttpStatus.BAD_REQUEST, "Bad Request", msg, TYPE_BAD_REQUEST, exchange);
    }

    // Your explicit argument failures (thrown by validators, etc.)
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(final IllegalArgumentException ex, final ServerWebExchange exchange) {
        logWarn(ex, exchange, "Bad request");
        return base(HttpStatus.BAD_REQUEST, "Bad Request", safeMessage(ex), TYPE_BAD_REQUEST, exchange);
    }

    // ResponseStatusException from Spring (carry their own status)
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleRse(final ResponseStatusException ex, final ServerWebExchange exchange) {
        logWarn(ex, exchange, "ResponseStatusException");
        final HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        return base(status != null ? status : HttpStatus.BAD_REQUEST,
            ex.getReason() != null ? ex.getReason() : "Error",
            safeMessage(ex),
            TYPE_BAD_REQUEST,
            exchange);
    }

    /* -------------------- 404: not found -------------------- */

    @ExceptionHandler(AdminAreaNotFoundException.class)
    public ProblemDetail handleNotFound(final AdminAreaNotFoundException ex, final ServerWebExchange exchange) {
        logInfo(ex, exchange, "Not found (404)");
        return base(HttpStatus.NOT_FOUND, "Not Found", safeMessage(ex), TYPE_NOT_FOUND, exchange);
    }

    /* -------------------- 409: domain conflicts -------------------- */

    @ExceptionHandler({
        DuplicateAdminAreaException.class,
        ParentNotFoundException.class,      // parent missing during create
        ChildrenExistException.class
    })
    public ProblemDetail handleConflict(final AdminAreaException ex, final ServerWebExchange exchange) {
        logInfo(ex, exchange, "Conflict (409)");
        return base(HttpStatus.CONFLICT, "Conflict", safeMessage(ex), TYPE_CONFLICT, exchange);
    }

    // DB-based conflicts (e.g., unique index violations)
    @ExceptionHandler({ DuplicateKeyException.class, DataIntegrityViolationException.class })
    public ProblemDetail handleDbConflict(final Exception ex, final ServerWebExchange exchange) {
        logInfo(ex, exchange, "DB conflict (409)");
        return base(HttpStatus.CONFLICT, "Conflict", "Data integrity violation", TYPE_CONFLICT, exchange);
    }

    /* -------------------- 415 / 406: content negotiation -------------------- */

    @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    public ProblemDetail handleUnsupportedMedia(final UnsupportedMediaTypeStatusException ex,
                                                final ServerWebExchange exchange) {
        logWarn(ex, exchange, "Unsupported media type");
        final String sent = ex.getContentType() != null ? ex.getContentType().toString() : "none";
        final String detail = "Content-Type '" + sent + "' is not supported. Use 'multipart/form-data' with a 'file' part.";
        final ProblemDetail pd = base(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", detail, TYPE_UNSUPPORTED, exchange);
        pd.setProperty("supported", ex.getSupportedMediaTypes().isEmpty()
            ? MediaType.MULTIPART_FORM_DATA_VALUE
            : ex.getSupportedMediaTypes().toString());
        return pd;
    }

    @ExceptionHandler(NotAcceptableStatusException.class)
    public ProblemDetail handleNotAcceptable(final NotAcceptableStatusException ex, final ServerWebExchange exchange) {
        logWarn(ex, exchange, "Not acceptable");
        final ProblemDetail pd = base(HttpStatus.NOT_ACCEPTABLE, "Not Acceptable",
            "Requested media type in Accept header is not supported.", TYPE_NOT_ACCEPTED, exchange);
        pd.setProperty("supported", ex.getSupportedMediaTypes().toString());
        return pd;
    }

    /* -------------------- Fallbacks -------------------- */

    // Treat generic IllegalStateException as conflict (until fully migrated off it)
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(final IllegalStateException ex, final ServerWebExchange exchange) {
        logInfo(ex, exchange, "IllegalState -> 409");
        return base(HttpStatus.CONFLICT, "Conflict", safeMessage(ex), TYPE_CONFLICT, exchange);
    }

    @ExceptionHandler(Throwable.class)
    public ProblemDetail handleUnexpected(final Throwable ex, final ServerWebExchange exchange) {
        log.error("Unhandled exception", ex);
        final ProblemDetail pd = base(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            "Unexpected error occurred", TYPE_INTERNAL, exchange);
        pd.setProperty("exception", ex.getClass().getName());
        if (ex.getCause() != null) {
            pd.setProperty("cause", ex.getCause().getClass().getName() + ": " + ex.getCause().getMessage());
        }
        return pd;
    }

    /* -------------------- helpers -------------------- */

    private ProblemDetail base(final HttpStatus status,
                               final String title,
                               final String detail,
                               final URI type,
                               final ServerWebExchange exchange) {
        final ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail != null ? detail : "");
        pd.setTitle(title);
        pd.setType(type);
        if (exchange != null && exchange.getRequest() != null) {
            pd.setInstance(exchange.getRequest().getURI()); // RFC-7807 "instance"
            pd.setProperty("requestId", exchange.getRequest().getId());
            if (exchange.getRequest().getMethod() != null) {
                pd.setProperty("method", exchange.getRequest().getMethod().name());
            }
        }
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    private String safeMessage(final Throwable ex) {
        return ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
    }

    private void logWarn(final Throwable ex, final ServerWebExchange exchange, final String msg) {
        if (exchange != null && exchange.getRequest() != null) {
            log.warn("[{}] {} {} - {}: {}", 
                exchange.getRequest().getId(),
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                msg,
                ex.toString());
        } else {
            log.warn("{}: {}", msg, ex.toString());
        }
    }

    private void logInfo(final Throwable ex, final ServerWebExchange exchange, final String msg) {
        if (exchange != null && exchange.getRequest() != null) {
            log.info("[{}] {} {} - {}", 
                exchange.getRequest().getId(),
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                msg);
        } else {
            log.info(msg);
        }
        log.debug("Cause: ", ex);
    }
}
