package com.piseth.java.school.addressservice.exception;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    private static final URI TYPE_VALIDATION   = URI.create("urn:problem-type:validation-error");
    private static final URI TYPE_BAD_REQUEST  = URI.create("urn:problem-type:bad-request");
    private static final URI TYPE_NOT_FOUND    = URI.create("urn:problem-type:not-found");
    private static final URI TYPE_CONFLICT     = URI.create("urn:problem-type:conflict");
    private static final URI TYPE_UNSUPPORTED  = URI.create("urn:problem-type:unsupported-media-type");
    private static final URI TYPE_NOT_ACCEPTED = URI.create("urn:problem-type:not-acceptable");
    private static final URI TYPE_INTERNAL     = URI.create("urn:problem-type:internal-error");

    // 415 — Wrong Content-Type (e.g., application/json sent to multipart endpoint)
    @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    public ProblemDetail handleUnsupportedMedia(final UnsupportedMediaTypeStatusException ex,
                                               final ServerWebExchange exchange) {
        logWarn(ex, exchange, "Unsupported media type");
        final String sent = ex.getContentType() != null ? ex.getContentType().toString() : "none";
        final String supported = ex.getSupportedMediaTypes().isEmpty()
            ? MediaType.MULTIPART_FORM_DATA_VALUE
            : ex.getSupportedMediaTypes().toString();

        final String detail = "Content-Type '" + sent + "' is not supported. Use 'multipart/form-data' with a 'file' part.";
        final ProblemDetail pd = base(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", detail, TYPE_UNSUPPORTED, exchange);
        pd.setProperty("supported", supported);
        return pd;
    }

    // 406 — Client Accept header can’t be satisfied
    @ExceptionHandler(NotAcceptableStatusException.class)
    public ProblemDetail handleNotAcceptable(final NotAcceptableStatusException ex, final ServerWebExchange exchange) {
        logWarn(ex, exchange, "Not acceptable");
        final ProblemDetail pd = base(HttpStatus.NOT_ACCEPTABLE, "Not Acceptable",
            "Requested media type in Accept header is not supported.", TYPE_NOT_ACCEPTED, exchange);
        pd.setProperty("supported", ex.getSupportedMediaTypes().toString());
        return pd;
    }

    // 400 — Body validation errors (@Valid on request bodies)
    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleBind(final WebExchangeBindException ex, final ServerWebExchange exchange) {
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
            })
            .collect(Collectors.toList());

        final ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Validation Failed", "Request validation failed", TYPE_VALIDATION, exchange);
        pd.setProperty("errors", errors);
        return pd;
    }

    // 400 — Param validation errors (@Validated on method params)
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
            })
            .collect(Collectors.toList());

        final ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Validation Failed", "Parameter validation failed", TYPE_VALIDATION, exchange);
        pd.setProperty("errors", violations);
        return pd;
    }

    // 400 — Malformed JSON / decoding issues
    @ExceptionHandler({ ServerWebInputException.class, DecodingException.class, CodecException.class })
    public ProblemDetail handleBadJson(final Exception ex, final ServerWebExchange exchange) {
        logWarn(ex, exchange, "Bad request payload");
        final String msg = ex.getMessage() != null ? ex.getMessage() : "Malformed request";
        return base(HttpStatus.BAD_REQUEST, "Bad Request", msg, TYPE_BAD_REQUEST, exchange);
    }

    // 400 — Your explicit argument failures
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(final IllegalArgumentException ex, final ServerWebExchange exchange) {
        logWarn(ex, exchange, "Bad request");
        final String msg = ex.getMessage() != null ? ex.getMessage() : "Bad request";
        return base(HttpStatus.BAD_REQUEST, "Bad Request", msg, TYPE_BAD_REQUEST, exchange);
    }

    // 404 / 409 — Domain state conflicts
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(final IllegalStateException ex, final ServerWebExchange exchange) {
        final String message = ex.getMessage() != null ? ex.getMessage() : "";
        final String lower = message.toLowerCase(Locale.ROOT);

        final HttpStatus status;
        final URI type;
        final String title;

        if (lower.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
            type = TYPE_NOT_FOUND;
            title = "Not Found";
        } else if (lower.contains("already exists") || lower.contains("children exist") || lower.contains("conflict")) {
            status = HttpStatus.CONFLICT;
            type = TYPE_CONFLICT;
            title = "Conflict";
        } else {
            status = HttpStatus.CONFLICT;
            type = TYPE_CONFLICT;
            title = "Conflict";
        }

        logInfo(ex, exchange, "Business state error -> {}", status.value());
        return base(status, title, message, type, exchange);
    }

    // 500 — Fallback
    @ExceptionHandler(Throwable.class)
    public ProblemDetail handleUnexpected(final Throwable ex, final ServerWebExchange exchange) {
        logError(ex, exchange, "Unhandled exception");
        final ProblemDetail pd = base(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            "Unexpected error occurred", TYPE_INTERNAL, exchange);
        pd.setProperty("exception", ex.getClass().getName());
        if (ex.getCause() != null) {
            pd.setProperty("cause", ex.getCause().getClass().getName() + ": " + ex.getCause().getMessage());
        }
        return pd;
    }

    /* ---------- helpers ---------- */

    private ProblemDetail base(final HttpStatus status,
                               final String title,
                               final String detail,
                               final URI type,
                               final ServerWebExchange exchange) {
        final ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail != null ? detail : "");
        pd.setTitle(title);
        pd.setType(type);
        if (exchange != null && exchange.getRequest() != null) {
            pd.setInstance(exchange.getRequest().getURI()); // RFC-7807 field
            pd.setProperty("requestId", exchange.getRequest().getId());
            // Removed "path" to avoid duplication with "instance".
            // Keep method optionally (handy for logs/clients).
            if (exchange.getRequest().getMethod() != null) {
                pd.setProperty("method", exchange.getRequest().getMethod().name());
            }
        }
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
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

    private void logInfo(final Throwable ex, final ServerWebExchange exchange, final String fmt, final Object arg) {
        if (exchange != null && exchange.getRequest() != null) {
            log.info("[{}] {} {} - " + fmt + " ({})",
                exchange.getRequest().getId(),
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                arg,
                ex.toString());
        } else {
            log.info(fmt + " ({})", arg, ex.toString());
        }
    }

    private void logError(final Throwable ex, final ServerWebExchange exchange, final String msg) {
        if (exchange != null && exchange.getRequest() != null) {
            log.error("[{}] {} {} - {}",
                exchange.getRequest().getId(),
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                msg,
                ex);
        } else {
            log.error(msg, ex);
        }
    }
}
