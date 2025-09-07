// src/main/java/com/piseth/java/school/addressservice/exception/GlobalErrorHandler.java
package com.piseth.java.school.addressservice.exception;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI TYPE_VALIDATION = URI.create("urn:problem-type:validation-error");
    private static final URI TYPE_BAD_REQUEST = URI.create("urn:problem-type:bad-request");
    private static final URI TYPE_NOT_FOUND  = URI.create("urn:problem-type:not-found");
    private static final URI TYPE_CONFLICT   = URI.create("urn:problem-type:conflict");
    private static final URI TYPE_INTERNAL   = URI.create("urn:problem-type:internal-error");

    // 400 — Body validation errors (@Valid on request bodies)
    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleBind(final WebExchangeBindException ex, final ServerWebExchange exchange) {
        final List<Map<String, Object>> errors = ex.getFieldErrors()
            .stream()
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

        final ProblemDetail pd = base(HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Request validation failed",
                TYPE_VALIDATION,
                exchange);
        pd.setProperty("errors", errors);
        return pd;
    }

    // 400 — Query/path param validation errors (e.g., @Validated on controller method params)
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(final ConstraintViolationException ex, final ServerWebExchange exchange) {
        final List<Map<String, Object>> violations = ex.getConstraintViolations()
            .stream()
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

        final ProblemDetail pd = base(HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Parameter validation failed",
                TYPE_VALIDATION,
                exchange);
        pd.setProperty("errors", violations);
        return pd;
    }

    // 400 — Malformed JSON / body decoding issues
    @ExceptionHandler({ ServerWebInputException.class, DecodingException.class, CodecException.class })
    public ProblemDetail handleBadJson(final Exception ex, final ServerWebExchange exchange) {
        final String msg = ex.getMessage() != null ? ex.getMessage() : "Malformed request";
        final ProblemDetail pd = base(HttpStatus.BAD_REQUEST,
                "Bad Request",
                msg,
                TYPE_BAD_REQUEST,
                exchange);
        return pd;
    }

    // 400 — API precondition failures / bad arguments
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(final IllegalArgumentException ex, final ServerWebExchange exchange) {
        final ProblemDetail pd = base(HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage() != null ? ex.getMessage() : "Bad request",
                TYPE_BAD_REQUEST,
                exchange);
        return pd;
    }

    // 404 / 409 — Domain state errors
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

        final ProblemDetail pd = base(status, title, message, type, exchange);
        return pd;
    }

    // 500 — Fallback
    @ExceptionHandler(Throwable.class)
    public ProblemDetail handleUnexpected(final Throwable ex, final ServerWebExchange exchange) {
        final ProblemDetail pd = base(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Unexpected error occurred",
                TYPE_INTERNAL,
                exchange);
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
        if (exchange != null) {
            pd.setInstance(exchange.getRequest().getURI());
            pd.setProperty("requestId", exchange.getRequest().getId());
        }
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
